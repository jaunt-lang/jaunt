/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/
/* rich Aug 21, 2007 */
package clojure.lang;
import clojure.asm.*;
import clojure.asm.commons.GeneratorAdapter;
import clojure.asm.commons.Method;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
public class Compiler implements Opcodes{
static final String COMPILE_STUB_PREFIX = "compile__stub";
static final Symbol NS = Symbol.intern("ns");
static final Symbol IN_NS = Symbol.intern("in-ns");

static final public Var LOADER = Var.create().setDynamic();
static final public Var REFER = Var.intern(Namespace.findOrCreate(Symbol.intern("clojure.core")),
                                           Symbol.intern("refer"));
static final public Var REQUIRE = Var.intern(Namespace.findOrCreate(Symbol.intern("clojure.core")),
                                             Symbol.intern("require"));
static final public Var IMPORT_VAR = Var.intern(Namespace.findOrCreate(Symbol.intern("clojure.core")),
                                             Symbol.intern("import*"));
static final Keyword macroKey = Keyword.intern(null, "macro");
static public String getNSClassname(Namespace ns) {
	return "L"+munge(ns.toString()).replace(".", "/")+"__init;";
}

static final public Var COMPILE_STUB_SYM = Var.create(null).setDynamic();
static final public Var COMPILE_STUB_CLASS = Var.create(null).setDynamic();
static final public Var COMPILE_FILES = Var.intern(Namespace.findOrCreate(Symbol.intern("clojure.core")),
                                                   Symbol.intern("*compile-files*"), Boolean.FALSE).setDynamic();
static final public Var LINE = Var.create(0).setDynamic();
static final public Var COLUMN = Var.create(0).setDynamic();

static final Symbol DEF = Symbol.intern("def");
static final Symbol LOOP = Symbol.intern("loop*");
static final Symbol RECUR = Symbol.intern("recur");
static final Symbol IF = Symbol.intern("if");
static final Symbol LET = Symbol.intern("let*");
static final Symbol LETFN = Symbol.intern("letfn*");
static final Symbol DO = Symbol.intern("do");
static final Symbol FN = Symbol.intern("fn*");
// static final Symbol FNONCE = (Symbol) Symbol.intern("fn*").withMeta(RT.map(Keyword.intern(null, "once"), RT.T));
static final Symbol QUOTE = Symbol.intern("quote");
static final Symbol THE_VAR = Symbol.intern("var");
static final Symbol DOT = Symbol.intern(".");
static final Symbol ASSIGN = Symbol.intern("set!");
//static final Symbol TRY_FINALLY = Symbol.intern("try-finally");
static final Symbol TRY = Symbol.intern("try");
static final Symbol CATCH = Symbol.intern("catch");
static final Symbol FINALLY = Symbol.intern("finally");
static final Symbol THROW = Symbol.intern("throw");
static final Symbol MONITOR_ENTER = Symbol.intern("monitor-enter");
static final Symbol MONITOR_EXIT = Symbol.intern("monitor-exit");
static final Symbol IMPORT = Symbol.intern("clojure.core", "import*");
//static final Symbol INSTANCE = Symbol.intern("instance?");
static final Symbol DEFTYPE = Symbol.intern("deftype*");
static final Symbol CASE = Symbol.intern("case*");
static final Symbol _AMP_ = Symbol.intern("&");
static final Symbol REIFY = Symbol.intern("reify*");

static final public IPersistentMap specials = PersistentHashMap.create(
		DEF, null,
		LOOP, null,
		RECUR, null,
		IF, null,
		CASE, null,
		LET, null,
		LETFN, null,
		DO, null,
		FN, null,
		QUOTE, null,
		THE_VAR, null,
		IMPORT, null,
		DOT, null,
		ASSIGN, null,
		DEFTYPE, null,
		REIFY, null,
        TRY, null,
        THROW, null,
        MONITOR_ENTER, null,
        MONITOR_EXIT, null,
        CATCH, null,
        FINALLY, null,
        NEW, null,
        _AMP_, null
);

public enum C{
	STATEMENT,  //value ignored
	EXPRESSION, //value required
	RETURN,      //tail position relative to enclosing recur frame
	EVAL
}

interface Expr{
	Object eval() ;

	void emit(C context, ObjExpr objx, GeneratorAdapter gen);

	boolean hasJavaClass() ;

	Class getJavaClass() ;
}

static public class ObjExpr implements Expr{

	public Object eval() {
        return null;
	}

	public void emit(C context, ObjExpr objx, GeneratorAdapter gen){
	}

	public boolean hasJavaClass() {
		return true;
	}

	public Class getJavaClass() {
		return IFn.class;
	}

}

static public interface MaybePrimitiveExpr extends Expr{
	public boolean canEmitPrimitive();
	public void emitUnboxed(C context, ObjExpr objx, GeneratorAdapter gen);
}

static public abstract class HostExpr implements Expr, MaybePrimitiveExpr{

	public static void emitBoxReturn(ObjExpr objx, GeneratorAdapter gen, Class returnType){
	}

	public static void emitUnboxArg(ObjExpr objx, GeneratorAdapter gen, Class paramType){
	}

}

static Symbol resolveSymbol(Symbol sym){
	//already qualified or classname?
	if(sym.name.indexOf('.') > 0)
		return sym;
	if(sym.ns != null)
		{
		Namespace ns = namespaceFor(sym);
		if(ns == null || (ns.name.name == null ? sym.ns == null : ns.name.name.equals(sym.ns)))
			return sym;
		return Symbol.intern(ns.name.name, sym.name);
		}
	Object o = currentNS().getMapping(sym);
	if(o == null)
		return Symbol.intern(currentNS().name.name, sym.name);
	else if(o instanceof Class)
		return Symbol.intern(null, ((Class) o).getName());
	else if(o instanceof Var)
			{
			Var v = (Var) o;
			return Symbol.intern(v.ns.name.name, v.sym.name);
			}
	return null;
}

static final public IPersistentMap CHAR_MAP =
		PersistentHashMap.create('-', "_",
':', "_COLON_",
'+', "_PLUS_",
'>', "_GT_",
'<', "_LT_",
'=', "_EQ_",
'~', "_TILDE_",
'!', "_BANG_",
'@', "_CIRCA_",
'#', "_SHARP_",
'\'', "_SINGLEQUOTE_",
'"', "_DOUBLEQUOTE_",
'%', "_PERCENT_",
'^', "_CARET_",
'&', "_AMPERSAND_",
'*', "_STAR_",
'|', "_BAR_",
'{', "_LBRACE_",
'}', "_RBRACE_",
'[', "_LBRACK_",
']', "_RBRACK_",
'/', "_SLASH_",
'\\', "_BSLASH_",
'?', "_QMARK_");
static final public IPersistentMap DEMUNGE_MAP;
static final public Pattern DEMUNGE_PATTERN;
static {
	// DEMUNGE_MAP maps strings to characters in the opposite
	// direction that CHAR_MAP does, plus it maps "$" to '/'
	IPersistentMap m = RT.map("$", '/');
	for(ISeq s = RT.seq(CHAR_MAP); s != null; s = s.next())
		{
		IMapEntry e = (IMapEntry) s.first();
		Character origCh = (Character) e.key();
		String escapeStr = (String) e.val();
		m = m.assoc(escapeStr, origCh);
		}
	DEMUNGE_MAP = m;
	// DEMUNGE_PATTERN searches for the first of any occurrence of
	// the strings that are keys of DEMUNGE_MAP.
	// Note: Regex matching rules mean that #"_|_COLON_" "_COLON_"
       // returns "_", but #"_COLON_|_" "_COLON_" returns "_COLON_"
       // as desired.  Sorting string keys of DEMUNGE_MAP from longest to
       // shortest ensures correct matching behavior, even if some strings are
	// prefixes of others.
	Object[] mungeStrs = RT.toArray(RT.keys(m));
	Arrays.sort(mungeStrs, new Comparator() {
                public int compare(Object s1, Object s2) {
                    return ((String) s2).length() - ((String) s1).length();
                }});
	StringBuilder sb = new StringBuilder();
	boolean first = true;
	for(Object s : mungeStrs)
		{
		String escapeStr = (String) s;
		if (!first)
			sb.append("|");
		first = false;
		sb.append("\\Q");
		sb.append(escapeStr);
		sb.append("\\E");
		}
	DEMUNGE_PATTERN = Pattern.compile(sb.toString());
}
static public String munge(String name){
	StringBuilder sb = new StringBuilder();
	for(char c : name.toCharArray())
		{
		String sub = (String) CHAR_MAP.valAt(c);
		if(sub != null)
			sb.append(sub);
		else
			sb.append(c);
		}
	return sb.toString();
}
static public String demunge(String mungedName){
	StringBuilder sb = new StringBuilder();
	Matcher m = DEMUNGE_PATTERN.matcher(mungedName);
	int lastMatchEnd = 0;
	while (m.find())
		{
		int start = m.start();
		int end = m.end();
		// Keep everything before the match
		sb.append(mungedName.substring(lastMatchEnd, start));
		lastMatchEnd = end;
		// Replace the match with DEMUNGE_MAP result
		Character origCh = (Character) DEMUNGE_MAP.valAt(m.group());
		sb.append(origCh);
		}
	// Keep everything after the last match
	sb.append(mungedName.substring(lastMatchEnd));
	return sb.toString();
}

static public class CompilerException extends RuntimeException{
	final public String source;
	
	final public int line;
	public CompilerException(String source, int line, int column, Throwable cause){
		super(errorMsg(source, line, column, cause.toString()), cause);
		this.source = source;
		this.line = line;
	}
	public String toString(){
		return getMessage();
	}
}
static String errorMsg(String source, int line, int column, String s){
	return String.format("%s, compiling:(%s:%d:%d)", s, source, line, column);
}
public static Object eval(Object form) {
	return eval(form, true);
}
public static Object eval(Object form, boolean freshLoader) {
    return null;
}
static String destubClassName(String className){
	//skip over prefix + '.' or '/'
	if(className.startsWith(COMPILE_STUB_PREFIX))
		return className.substring(COMPILE_STUB_PREFIX.length()+1);
	return className;
}
static Type getType(Class c){
	String descriptor = Type.getType(c).getDescriptor();
	if(descriptor.startsWith("L"))
		descriptor = "L" + destubClassName(descriptor.substring(1));
	return Type.getType(descriptor);
}
static Object resolve(Symbol sym, boolean allowPrivate) {
	return resolveIn(currentNS(), sym, allowPrivate);
}
static Object resolve(Symbol sym) {
	return resolveIn(currentNS(), sym, false);
}
static Namespace namespaceFor(Symbol sym){
	return namespaceFor(currentNS(), sym);
}
static Namespace namespaceFor(Namespace inns, Symbol sym){
	//note, presumes non-nil sym.ns
	// first check against currentNS' aliases...
	Symbol nsSym = Symbol.intern(sym.ns);
	Namespace ns = inns.lookupAlias(nsSym);
	if(ns == null)
		{
		// ...otherwise check the Namespaces map.
		ns = Namespace.find(nsSym);
		}
	return ns;
}
static public Object resolveIn(Namespace n, Symbol sym, boolean allowPrivate) {
	//note - ns-qualified vars must already exist
	if(sym.ns != null)
		{
		Namespace ns = namespaceFor(n, sym);
		if(ns == null)
			throw Util.runtimeException("No such namespace: " + sym.ns);
		Var v = ns.findInternedVar(Symbol.intern(sym.name));
		if(v == null)
			throw Util.runtimeException("No such var: " + sym);
		else if(v.ns != currentNS() && !v.isPublic() && !allowPrivate)
			throw new IllegalStateException("var: " + sym + " is not public");
		return v;
		}
	else if(sym.name.indexOf('.') > 0 || sym.name.charAt(0) == '[')
		{
		return RT.classForName(sym.name);
		}
	else if(sym.equals(NS))
			return RT.NS_VAR;
	else if(sym.equals(IN_NS))
			return RT.IN_NS_VAR;
	else
		{
		if(Util.equals(sym, COMPILE_STUB_SYM.get()))
			return COMPILE_STUB_CLASS.get();
		Object o = n.getMapping(sym);
		if(o == null)
			{
			if(RT.booleanCast(RT.ALLOW_UNRESOLVED_VARS.deref()))
				{
				return sym;
				}
			else
				{
				throw Util.runtimeException("Unable to resolve symbol: " + sym + " in this context");
				}
			}
		return o;
		}
}
static public Object maybeResolveIn(Namespace n, Symbol sym) {
	//note - ns-qualified vars must already exist
	if(sym.ns != null)
		{
		Namespace ns = namespaceFor(n, sym);
		if(ns == null)
			return null;
		Var v = ns.findInternedVar(Symbol.intern(sym.name));
		if(v == null)
			return null;
		return v;
		}
	else if(sym.name.indexOf('.') > 0 && !sym.name.endsWith(".") 
			|| sym.name.charAt(0) == '[')
		{
		return RT.classForName(sym.name);
		}
	else if(sym.equals(NS))
			return RT.NS_VAR;
		else if(sym.equals(IN_NS))
				return RT.IN_NS_VAR;
			else
				{
				Object o = n.getMapping(sym);
				return o;
				}
}
static Namespace currentNS(){
	return (Namespace) RT.CURRENT_NS.deref();
}
public static Object loadFile(String file) throws IOException{
    return null;
}
public static Object load(Reader rdr) {
	// return load(rdr, null, "NO_SOURCE_FILE");
    return null;
}
public static Object load(Reader rdr, String sourcePath, String sourceName) {
    return null;
}
static public void writeClassFile(String internalName, byte[] bytecode) throws IOException{
}
public static void pushNS(){
	Var.pushThreadBindings(PersistentHashMap.create(Var.intern(Symbol.intern("clojure.core"),
	                                                           Symbol.intern("*ns*")).setDynamic(), null));
}
public static ILookupThunk getLookupThunk(Object target, Keyword k){
	return null;  //To change body of created methods use File | Settings | File Templates.
}
public static Object compile(Reader rdr, String sourcePath, String sourceName) throws IOException{
    return null;
}

public static Object macroexpand1(Object x) {
    return null;
}

static boolean isSpecial(Object sym){
	return specials.containsKey(sym);
}

public static boolean namesStaticMember(Symbol sym){
	return sym.ns != null && namespaceFor(sym) == null;
}

static public boolean subsumes(Class[] c1, Class[] c2){
	//presumes matching lengths
	Boolean better = false;
	for(int i = 0; i < c1.length; i++)
		{
		if(c1[i] != c2[i])
			{
			if(!c1[i].isPrimitive() && c2[i].isPrimitive()
			   ||
			   c2[i].isAssignableFrom(c1[i]))
				better = true;
			else
				return false;
			}
		}
	return better;
}

}
