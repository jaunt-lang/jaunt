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
// 	public final String name(){
// 		return name;
// 	}

// //	public final String simpleName(){
// //		return simpleName;
// //	}

// 	public final String internalName(){
// 		return internalName;
// 	}

// 	public final String thisName(){
// 		return thisName;
// 	}

// 	public final Type objtype(){
// 		return objtype;
// 	}

// 	public final IPersistentMap closes(){
// 		return closes;
// 	}

// 	public final IPersistentMap keywords(){
// 		return keywords;
// 	}

// 	public final IPersistentMap vars(){
// 		return vars;
// 	}

// 	public final Class compiledClass(){
// 		return compiledClass;
// 	}

// 	public final int line(){
// 		return line;
// 	}

// 	public final int column(){
// 		return column;
// 	}

// 	public final PersistentVector constants(){
// 		return constants;
// 	}

// 	public final int constantsID(){
// 		return constantsID;
// 	}

// 	final static Method kwintern = Method.getMethod("clojure.lang.Keyword intern(String, String)");
// 	final static Method symintern = Method.getMethod("clojure.lang.Symbol intern(String)");
// 	final static Method varintern =
// 			Method.getMethod("clojure.lang.Var intern(clojure.lang.Symbol, clojure.lang.Symbol)");

// 	final static Type DYNAMIC_CLASSLOADER_TYPE = Type.getType(DynamicClassLoader.class);
// 	final static Method getClassMethod = Method.getMethod("Class getClass()");
// 	final static Method getClassLoaderMethod = Method.getMethod("ClassLoader getClassLoader()");
// 	final static Method getConstantsMethod = Method.getMethod("Object[] getConstants(int)");
// 	final static Method readStringMethod = Method.getMethod("Object readString(String)");

// 	final static Type ILOOKUP_SITE_TYPE = Type.getType(ILookupSite.class);
// 	final static Type ILOOKUP_THUNK_TYPE = Type.getType(ILookupThunk.class);
// 	final static Type KEYWORD_LOOKUPSITE_TYPE = Type.getType(KeywordLookupSite.class);

// 	private DynamicClassLoader loader;
// 	private byte[] bytecode;

// 	public ObjExpr(Object tag){
// 		this.tag = tag;
// 	}

// 	static String trimGenID(String name){
// 		int i = name.lastIndexOf("__");
// 		return i==-1?name:name.substring(0,i);
// 	}
	


// 	Type[] ctorTypes(){
// 		IPersistentVector tv = !supportsMeta()?PersistentVector.EMPTY:RT.vector(IPERSISTENTMAP_TYPE);
// 		for(ISeq s = RT.keys(closes); s != null; s = s.next())
// 			{
// 			LocalBinding lb = (LocalBinding) s.first();
// 			if(lb.getPrimitiveType() != null)
// 				tv = tv.cons(Type.getType(lb.getPrimitiveType()));
// 			else
// 				tv = tv.cons(OBJECT_TYPE);
// 			}
// 		Type[] ret = new Type[tv.count()];
// 		for(int i = 0; i < tv.count(); i++)
// 			ret[i] = (Type) tv.nth(i);
// 		return ret;
// 	}

// 	void compile(String superName, String[] interfaceNames, boolean oneTimeUse) throws IOException{
// 		//create bytecode for a class
// 		//with name current_ns.defname[$letname]+
// 		//anonymous fns get names fn__id
// 		//derived from AFn/RestFn
// 		boolean emitLeanCode = RT.booleanCast(EMIT_LEAN_CODE.deref());
// 		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
// //		ClassWriter cw = new ClassWriter(0);
// 		ClassVisitor cv = cw;
// //		ClassVisitor cv = new TraceClassVisitor(new CheckClassAdapter(cw), new PrintWriter(System.out));
// 		//ClassVisitor cv = new TraceClassVisitor(cw, new PrintWriter(System.out));
// 		cv.visit(V1_5, ACC_PUBLIC + ACC_SUPER + ACC_FINAL, internalName, null,superName,interfaceNames);
// //		         superName != null ? superName :
// //		         (isVariadic() ? "clojure/lang/RestFn" : "clojure/lang/AFunction"), null);
// 		String source = (String) SOURCE.deref();
// 		int lineBefore = (Integer) LINE_BEFORE.deref();
// 		int lineAfter = (Integer) LINE_AFTER.deref() + 1;
// 		int columnBefore = (Integer) COLUMN_BEFORE.deref();
// 		int columnAfter = (Integer) COLUMN_AFTER.deref() + 1;

// 		if(source != null && SOURCE_PATH.deref() != null)
// 			{
// 			//cv.visitSource(source, null);
// 			String smap = "SMAP\n" +
// 			              ((source.lastIndexOf('.') > 0) ?
// 			               source.substring(0, source.lastIndexOf('.'))
// 			                :source)
// 			                       //                      : simpleName)
// 			              + ".java\n" +
// 			              "Clojure\n" +
// 			              "*S Clojure\n" +
// 			              "*F\n" +
// 			              "+ 1 " + source + "\n" +
// 			              (String) SOURCE_PATH.deref() + "\n" +
// 			              "*L\n" +
// 			              String.format("%d#1,%d:%d\n", lineBefore, lineAfter - lineBefore, lineBefore) +
// 			              "*E";
// 			cv.visitSource(source, smap);
// 			}
// 		addAnnotation(cv, classMeta);
// 		//static fields for constants
// 		for(int i = 0; i < constants.count(); i++)
// 			if (!emitLeanCode || !(Boolean)constantLeanFlags.nth(i))
// 			{
// 			cv.visitField(ACC_PUBLIC + ACC_FINAL
// 			              + ACC_STATIC, constantName(i), constantType(i).getDescriptor(),
// 			              null, null);
// 			}

// 		//static fields for lookup sites
// 		for(int i = 0; i < keywordCallsites.count(); i++)
// 			{
// 			cv.visitField(ACC_FINAL
// 			              + ACC_STATIC, siteNameStatic(i), KEYWORD_LOOKUPSITE_TYPE.getDescriptor(),
// 			              null, null);
// 			cv.visitField(ACC_STATIC, thunkNameStatic(i), ILOOKUP_THUNK_TYPE.getDescriptor(),
// 			              null, null);
// 			}

// //		for(int i=0;i<varCallsites.count();i++)
// //			{
// //			cv.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL
// //					, varCallsiteName(i), IFN_TYPE.getDescriptor(), null, null);
// //			}

// 		//static init for constants, keywords and vars
// 		GeneratorAdapter clinitgen = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC,
// 		                                                  Method.getMethod("void <clinit> ()"),
// 		                                                  null,
// 		                                                  null,
// 		                                                  cv);
// 		clinitgen.visitCode();
// 		clinitgen.visitLineNumber(line, clinitgen.mark());

// 		if(constants.count() > 0)
// 			{
// 			emitConstants(clinitgen);
// 			}

// 		if(keywordCallsites.count() > 0)
// 			emitKeywordCallsites(clinitgen);

// 		/*
// 		for(int i=0;i<varCallsites.count();i++)
// 			{
// 			Label skipLabel = clinitgen.newLabel();
// 			Label endLabel = clinitgen.newLabel();
// 			Var var = (Var) varCallsites.nth(i);
// 			clinitgen.push(var.ns.name.toString());
// 			clinitgen.push(var.sym.toString());
// 			clinitgen.invokeStatic(RT_TYPE, Method.getMethod("clojure.lang.Var var(String,String)"));
// 			clinitgen.dup();
// 			clinitgen.invokeVirtual(VAR_TYPE,Method.getMethod("boolean hasRoot()"));
// 			clinitgen.ifZCmp(GeneratorAdapter.EQ,skipLabel);

// 			clinitgen.invokeVirtual(VAR_TYPE,Method.getMethod("Object getRoot()"));
//             clinitgen.dup();
//             clinitgen.instanceOf(AFUNCTION_TYPE);
//             clinitgen.ifZCmp(GeneratorAdapter.EQ,skipLabel);
// 			clinitgen.checkCast(IFN_TYPE);
// 			clinitgen.putStatic(objtype, varCallsiteName(i), IFN_TYPE);
// 			clinitgen.goTo(endLabel);

// 			clinitgen.mark(skipLabel);
// 			clinitgen.pop();

// 			clinitgen.mark(endLabel);
// 			}
//         */

// 		/// This is our singleton implementation
// 		if (emitLeanCode && (superName.equals("clojure/lang/AFunction")
// 							 || superName.equals("clojure/lang/RestFn"))
// 			&& (ctorTypes().length == 0)) {
// 			clinitgen.newInstance(objtype);
// 			clinitgen.dup();
// 			clinitgen.invokeConstructor(objtype, Method.getMethod("void <init>()"));
// 			clinitgen.putStatic(objtype, "__instance", OBJECT_TYPE);
// 		}

// 		clinitgen.returnValue();

// 		clinitgen.endMethod();

// 		cv.visitField(ACC_PUBLIC + ACC_STATIC, "__instance", OBJECT_TYPE.getDescriptor(),
// 					  null, null);
// 		/// Singleton ends here

// 		if(supportsMeta())
// 			{
// 			cv.visitField(ACC_FINAL, "__meta", IPERSISTENTMAP_TYPE.getDescriptor(), null, null);
// 			}
// 		//instance fields for closed-overs
// 		for(ISeq s = RT.keys(closes); s != null; s = s.next())
// 			{
// 			LocalBinding lb = (LocalBinding) s.first();
// 			if(isDeftype())
// 				{
// 				int access = isVolatile(lb) ? ACC_VOLATILE :
// 				             isMutable(lb) ? 0 :
// 				             (ACC_PUBLIC + ACC_FINAL);
// 				FieldVisitor fv;
// 				if(lb.getPrimitiveType() != null)
// 					fv = cv.visitField(access
// 							, lb.name, Type.getType(lb.getPrimitiveType()).getDescriptor(),
// 								  null, null);
// 				else
// 				//todo - when closed-overs are fields, use more specific types here and in ctor and emitLocal?
// 					fv = cv.visitField(access
// 							, lb.name, OBJECT_TYPE.getDescriptor(), null, null);
// 				addAnnotation(fv, RT.meta(lb.sym));
// 				}
// 			else
// 				{
// 				//todo - only enable this non-private+writability for letfns where we need it
// 				if(lb.getPrimitiveType() != null)
// 					cv.visitField(0 + (isVolatile(lb) ? ACC_VOLATILE : 0)
// 							, lb.name, Type.getType(lb.getPrimitiveType()).getDescriptor(),
// 								  null, null);
// 				else
// 					cv.visitField(0 //+ (oneTimeUse ? 0 : ACC_FINAL)
// 							, lb.name, OBJECT_TYPE.getDescriptor(), null, null);
// 				}
// 			}

// 		//static fields for callsites and thunks
// 		for(int i=0;i<protocolCallsites.count();i++)
// 			{
// 			cv.visitField(ACC_PRIVATE + ACC_STATIC, cachedClassName(i), CLASS_TYPE.getDescriptor(), null, null);
// 			}

//  		//ctor that takes closed-overs and inits base + fields
// 		Method m = new Method("<init>", Type.VOID_TYPE, ctorTypes());
// 		GeneratorAdapter ctorgen = new GeneratorAdapter(ACC_PUBLIC,
// 		                                                m,
// 		                                                null,
// 		                                                null,
// 		                                                cv);
// 		Label start = ctorgen.newLabel();
// 		Label end = ctorgen.newLabel();
// 		ctorgen.visitCode();
// 		ctorgen.visitLineNumber(line, ctorgen.mark());
// 		ctorgen.visitLabel(start);
// 		ctorgen.loadThis();
// //		if(superName != null)
// 			ctorgen.invokeConstructor(Type.getObjectType(superName), voidctor);
// //		else if(isVariadic()) //RestFn ctor takes reqArity arg
// //			{
// //			ctorgen.push(variadicMethod.reqParms.count());
// //			ctorgen.invokeConstructor(restFnType, restfnctor);
// //			}
// //		else
// //			ctorgen.invokeConstructor(aFnType, voidctor);

// //		if(vars.count() > 0)
// //			{
// //			ctorgen.loadThis();
// //			ctorgen.getStatic(VAR_TYPE,"rev",Type.INT_TYPE);
// //			ctorgen.push(-1);
// //			ctorgen.visitInsn(Opcodes.IADD);
// //			ctorgen.putField(objtype, "__varrev__", Type.INT_TYPE);
// //			}

// 		if(supportsMeta())
// 			{
// 			ctorgen.loadThis();
// 			ctorgen.visitVarInsn(IPERSISTENTMAP_TYPE.getOpcode(Opcodes.ILOAD), 1);
// 			ctorgen.putField(objtype, "__meta", IPERSISTENTMAP_TYPE);
// 			}

// 		int a = supportsMeta()?2:1;
// 		for(ISeq s = RT.keys(closes); s != null; s = s.next(), ++a)
// 			{
// 			LocalBinding lb = (LocalBinding) s.first();
// 			ctorgen.loadThis();
// 			Class primc = lb.getPrimitiveType();
// 			if(primc != null)
// 				{
// 				ctorgen.visitVarInsn(Type.getType(primc).getOpcode(Opcodes.ILOAD), a);
// 				ctorgen.putField(objtype, lb.name, Type.getType(primc));
// 				if(primc == Long.TYPE || primc == Double.TYPE)
// 					++a;
// 				}
// 			else
// 				{
// 				ctorgen.visitVarInsn(OBJECT_TYPE.getOpcode(Opcodes.ILOAD), a);
// 				ctorgen.putField(objtype, lb.name, OBJECT_TYPE);
// 				}
// 			// No idea why we shouldn't call this in lean mode, need to sort it
// 			// out later.
// 			if (!emitLeanCode)
// 				closesExprs = closesExprs.cons(new LocalBindingExpr(lb, null));
// 			}


// 		ctorgen.visitLabel(end);

// 		ctorgen.returnValue();

// 		ctorgen.endMethod();

// 		if(altCtorDrops > 0)
// 			{
// 					//ctor that takes closed-overs and inits base + fields
// 			Type[] ctorTypes = ctorTypes();
// 			Type[] altCtorTypes = new Type[ctorTypes.length-altCtorDrops];
// 			for(int i=0;i<altCtorTypes.length;i++)
// 				altCtorTypes[i] = ctorTypes[i];
// 			Method alt = new Method("<init>", Type.VOID_TYPE, altCtorTypes);
// 			ctorgen = new GeneratorAdapter(ACC_PUBLIC,
// 															alt,
// 															null,
// 															null,
// 															cv);
// 			ctorgen.visitCode();
// 			ctorgen.loadThis();
// 			ctorgen.loadArgs();
// 			for(int i=0;i<altCtorDrops;i++)
// 				ctorgen.visitInsn(Opcodes.ACONST_NULL);

// 			ctorgen.invokeConstructor(objtype, new Method("<init>", Type.VOID_TYPE, ctorTypes));

// 			ctorgen.returnValue();
// 			ctorgen.endMethod();
// 			}

// 		if(supportsMeta())
// 			{
// 			//ctor that takes closed-overs but not meta
// 			Type[] ctorTypes = ctorTypes();
// 			Type[] noMetaCtorTypes = new Type[ctorTypes.length-1];
// 			for(int i=1;i<ctorTypes.length;i++)
// 				noMetaCtorTypes[i-1] = ctorTypes[i];
// 			Method alt = new Method("<init>", Type.VOID_TYPE, noMetaCtorTypes);
// 			ctorgen = new GeneratorAdapter(ACC_PUBLIC,
// 															alt,
// 															null,
// 															null,
// 															cv);
// 			ctorgen.visitCode();
// 			ctorgen.loadThis();
// 			ctorgen.visitInsn(Opcodes.ACONST_NULL);	//null meta
// 			ctorgen.loadArgs();
// 			ctorgen.invokeConstructor(objtype, new Method("<init>", Type.VOID_TYPE, ctorTypes));

// 			ctorgen.returnValue();
// 			ctorgen.endMethod();

// 			//meta()
// 			Method meth = Method.getMethod("clojure.lang.IPersistentMap meta()");

// 			GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC,
// 												meth,
// 												null,
// 												null,
// 												cv);
// 			gen.visitCode();
// 			gen.loadThis();
// 			gen.getField(objtype,"__meta",IPERSISTENTMAP_TYPE);

// 			gen.returnValue();
// 			gen.endMethod();

// 			//withMeta()
// 			meth = Method.getMethod("clojure.lang.IObj withMeta(clojure.lang.IPersistentMap)");

// 			gen = new GeneratorAdapter(ACC_PUBLIC,
// 												meth,
// 												null,
// 												null,
// 												cv);
// 			gen.visitCode();
// 			gen.newInstance(objtype);
// 			gen.dup();
// 			gen.loadArg(0);

// 			for(ISeq s = RT.keys(closes); s != null; s = s.next(), ++a)
// 				{
// 				LocalBinding lb = (LocalBinding) s.first();
// 				gen.loadThis();
// 				Class primc = lb.getPrimitiveType();
// 				if(primc != null)
// 					{
// 					gen.getField(objtype, lb.name, Type.getType(primc));
// 					}
// 				else
// 					{
// 					gen.getField(objtype, lb.name, OBJECT_TYPE);
// 					}
// 				}

// 			gen.invokeConstructor(objtype, new Method("<init>", Type.VOID_TYPE, ctorTypes));
// 			gen.returnValue();
// 			gen.endMethod();
// 			}

// 		emitStatics(cv);
// 		emitMethods(cv);

// 		if(keywordCallsites.count() > 0)
// 			{
// 			Method meth = Method.getMethod("void swapThunk(int,clojure.lang.ILookupThunk)");

// 			GeneratorAdapter gen = new GeneratorAdapter(ACC_PUBLIC,
// 												meth,
// 												null,
// 												null,
// 												cv);
// 			gen.visitCode();
// 			Label endLabel = gen.newLabel();

// 			Label[] labels = new Label[keywordCallsites.count()];
// 			for(int i = 0; i < keywordCallsites.count();i++)
// 				{
// 				labels[i] = gen.newLabel();
// 				}
// 			gen.loadArg(0);
// 			gen.visitTableSwitchInsn(0,keywordCallsites.count()-1,endLabel,labels);

// 			for(int i = 0; i < keywordCallsites.count();i++)
// 				{
// 				gen.mark(labels[i]);
// //				gen.loadThis();
// 				gen.loadArg(1);
// 				gen.putStatic(objtype, thunkNameStatic(i),ILOOKUP_THUNK_TYPE);
// 				gen.goTo(endLabel);
// 				}

// 			gen.mark(endLabel);

// 			gen.returnValue();
// 			gen.endMethod();
// 			}
		
// 		//end of class
// 		cv.visitEnd();

// 		if (emitLeanCode) {
// 			byte[] leanBytecode = cw.toByteArray();
// 			writeClassFile(internalName, leanBytecode);
// 		} else {
// 			bytecode = cw.toByteArray();
// 			if (RT.booleanCast(COMPILE_FILES.deref())
// 				&& !RT.booleanCast(IS_COMPILING_A_MACRO.deref())) {
// 				try {
// 					// Repeat compile() method but with lean code flag set.
// 					Var.pushThreadBindings(RT.map(EMIT_LEAN_CODE, true));
// 					compile(superName, interfaceNames, oneTimeUse);
// 				} finally {
// 					Var.popThreadBindings();
// 				}
// 			}
// 		}
// 	}

// 	private void emitKeywordCallsites(GeneratorAdapter clinitgen){
// 		for(int i=0;i<keywordCallsites.count();i++)
// 			{
// 			Keyword k = (Keyword) keywordCallsites.nth(i);
// 			clinitgen.newInstance(KEYWORD_LOOKUPSITE_TYPE);
// 			clinitgen.dup();
// 			emitValue(k,clinitgen);
// 			clinitgen.invokeConstructor(KEYWORD_LOOKUPSITE_TYPE,
// 			                            Method.getMethod("void <init>(clojure.lang.Keyword)"));
// 			clinitgen.dup();
// 			clinitgen.putStatic(objtype, siteNameStatic(i), KEYWORD_LOOKUPSITE_TYPE);
// 			clinitgen.putStatic(objtype, thunkNameStatic(i), ILOOKUP_THUNK_TYPE);
// 			}
// 	}

// 	protected void emitStatics(ClassVisitor gen){
// 	}

// 	protected void emitMethods(ClassVisitor gen){
// 	}

// 	void emitListAsObjectArray(Object value, GeneratorAdapter gen){
// 		gen.push(((List) value).size());
// 		gen.newArray(OBJECT_TYPE);
// 		int i = 0;
// 		for(Iterator it = ((List) value).iterator(); it.hasNext(); i++)
// 			{
// 			gen.dup();
// 			gen.push(i);
// 			emitValue(it.next(), gen);
// 			gen.arrayStore(OBJECT_TYPE);
// 			}
// 	}

// 	void emitValue(Object value, GeneratorAdapter gen){
// 		boolean partial = true;
// 		boolean emitLeanCode = RT.booleanCast(EMIT_LEAN_CODE.deref());
// 		//System.out.println(value.getClass().toString());

// 		if(value == null)
// 			gen.visitInsn(Opcodes.ACONST_NULL);
// 		else if(value instanceof String)
// 			{
// 			gen.push((String) value);
// 			}
// 		else if(value instanceof Boolean)
// 			{
// 			if(((Boolean) value).booleanValue())
// 				gen.getStatic(BOOLEAN_OBJECT_TYPE, "TRUE", BOOLEAN_OBJECT_TYPE);
// 			else
// 				gen.getStatic(BOOLEAN_OBJECT_TYPE,"FALSE",BOOLEAN_OBJECT_TYPE);
// 			}
// 		else if(value instanceof Integer)
// 			{
// 			gen.push(((Integer) value).intValue());
// 			gen.invokeStatic(Type.getType(Integer.class), Method.getMethod("Integer valueOf(int)"));
// 			}
// 		else if(value instanceof Long)
// 			{
// 			gen.push(((Long) value).longValue());
// 			gen.invokeStatic(Type.getType(Long.class), Method.getMethod("Long valueOf(long)"));
// 			}
// 		else if(value instanceof Double)
// 				{
// 				gen.push(((Double) value).doubleValue());
// 				gen.invokeStatic(Type.getType(Double.class), Method.getMethod("Double valueOf(double)"));
// 				}
// 		else if(value instanceof Character)
// 				{
// 				gen.push(((Character) value).charValue());
// 				gen.invokeStatic(Type.getType(Character.class), Method.getMethod("Character valueOf(char)"));
// 				}
// 		else if(value instanceof Class)
// 			{
// 			Class cc = (Class)value;
// 			if(cc.isPrimitive())
// 				{
// 				Type bt;
// 				if ( cc == boolean.class ) bt = Type.getType(Boolean.class);
// 				else if ( cc == byte.class ) bt = Type.getType(Byte.class);
// 				else if ( cc == char.class ) bt = Type.getType(Character.class);
// 				else if ( cc == double.class ) bt = Type.getType(Double.class);
// 				else if ( cc == float.class ) bt = Type.getType(Float.class);
// 				else if ( cc == int.class ) bt = Type.getType(Integer.class);
// 				else if ( cc == long.class ) bt = Type.getType(Long.class);
// 				else if ( cc == short.class ) bt = Type.getType(Short.class);
// 				else throw Util.runtimeException(
// 						"Can't embed unknown primitive in code: " + value);
// 				gen.getStatic( bt, "TYPE", Type.getType(Class.class) );
// 				}
// 			else
// 				{
// 				if (emitLeanCode) {
// 					gen.visitLdcInsn(Type.getType(cc));
// 				} else {
// 					gen.push(destubClassName(cc.getName()));
// 					gen.invokeStatic(RT_TYPE, Method.getMethod("Class classForName(String)"));
// 				}
// 				}
// 			}
// 		else if(value instanceof Symbol)
// 			{
// 			gen.push(((Symbol) value).ns);
// 			gen.push(((Symbol) value).name);
// 			gen.invokeStatic(Type.getType(Symbol.class),
// 							 Method.getMethod("clojure.lang.Symbol intern(String,String)"));
// 			}
// 		else if(value instanceof Keyword)
// 			{
// 			gen.push(((Keyword) value).sym.ns);
// 			gen.push(((Keyword) value).sym.name);
// 			gen.invokeStatic(RT_TYPE,
// 							 Method.getMethod("clojure.lang.Keyword keyword(String,String)"));
// 			}
// //						else if(value instanceof KeywordCallSite)
// //								{
// //								emitValue(((KeywordCallSite) value).k.sym, gen);
// //								gen.invokeStatic(Type.getType(KeywordCallSite.class),
// //								                 Method.getMethod("clojure.lang.KeywordCallSite create(clojure.lang.Symbol)"));
// //								}
// 		else if(value instanceof Var)
// 			{
// 			Var var = (Var) value;
// 			gen.push(var.ns.name.toString());
// 			gen.push(var.sym.toString());
// 			gen.invokeStatic(RT_TYPE, Method.getMethod("clojure.lang.Var var(String,String)"));
// 			}
// 		else if(value instanceof IType)
// 			{
// 			Method ctor = new Method("<init>", Type.getConstructorDescriptor(value.getClass().getConstructors()[0]));
// 			gen.newInstance(Type.getType(value.getClass()));
// 			gen.dup();
// 			IPersistentVector fields = (IPersistentVector) Reflector.invokeStaticMethod(value.getClass(), "getBasis", new Object[]{});
// 			for(ISeq s = RT.seq(fields); s != null; s = s.next())
// 				{
// 				Symbol field = (Symbol) s.first();
// 				Class k = tagClass(tagOf(field));
// 				Object val = Reflector.getInstanceField(value, field.name);
// 				emitValue(val, gen);

// 				if(k.isPrimitive())
// 					{
// 					Type b = Type.getType(boxClass(k));
// 					String p = Type.getType(k).getDescriptor();
// 					String n = k.getName();

// 					gen.invokeVirtual(b, new Method(n+"Value", "()"+p));
// 					}
// 				}
// 			gen.invokeConstructor(Type.getType(value.getClass()), ctor);
// 			}
// 		else if(value instanceof IRecord)
// 			{
// 			Method createMethod = Method.getMethod(value.getClass().getName() + " create(clojure.lang.IPersistentMap)");
//             emitValue(PersistentArrayMap.create((java.util.Map) value), gen);
// 			gen.invokeStatic(getType(value.getClass()), createMethod);
// 			}
// 		else if(value instanceof IPersistentMap)
// 			{
// 			List entries = new ArrayList();
// 			for(Map.Entry entry : (Set<Map.Entry>) ((Map) value).entrySet())
// 				{
// 				entries.add(entry.getKey());
// 				entries.add(entry.getValue());
// 				}
// 			emitListAsObjectArray(entries, gen);
// 			gen.invokeStatic(RT_TYPE,
// 							 Method.getMethod("clojure.lang.IPersistentMap map(Object[])"));
// 			}
// 		else if(value instanceof IPersistentVector)
// 			{
// 			emitListAsObjectArray(value, gen);
// 			gen.invokeStatic(RT_TYPE, Method.getMethod(
// 					"clojure.lang.IPersistentVector vector(Object[])"));
// 			}
// 		else if(value instanceof PersistentHashSet)
// 			{
// 			ISeq vs = RT.seq(value);
// 			if(vs == null)
// 				gen.getStatic(Type.getType(PersistentHashSet.class),"EMPTY",Type.getType(PersistentHashSet.class));
// 			else
// 				{
// 				emitListAsObjectArray(vs, gen);
// 				gen.invokeStatic(Type.getType(PersistentHashSet.class), Method.getMethod(
// 					"clojure.lang.PersistentHashSet create(Object[])"));
// 				}
// 			}
// 		else if(value instanceof ISeq || value instanceof IPersistentList)
// 			{
// 			emitListAsObjectArray(value, gen);
// 			gen.invokeStatic(Type.getType(java.util.Arrays.class),
// 							 Method.getMethod("java.util.List asList(Object[])"));
// 			gen.invokeStatic(Type.getType(PersistentList.class),
// 							 Method.getMethod(
// 									 "clojure.lang.IPersistentList create(java.util.List)"));
// 			}
// 		else if(value instanceof Pattern)
// 			{
// 			emitValue(value.toString(), gen);
// 			gen.invokeStatic(Type.getType(Pattern.class),
// 							 Method.getMethod("java.util.regex.Pattern compile(String)"));
// 			}
// 		else
// 			{
// 			String cs = null;
// 			try
// 				{
// 				cs = RT.printString(value);
// //				System.out.println("WARNING SLOW CODE: " + Util.classOf(value) + " -> " + cs);
// 				}
// 			catch(Exception e)
// 				{
// 				throw Util.runtimeException(
// 						"Can't embed object in code, maybe print-dup not defined: " +
// 						value);
// 				}
// 			if(cs.length() == 0)
// 				throw Util.runtimeException(
// 						"Can't embed unreadable object in code: " + value);

// 			if(cs.startsWith("#<"))
// 				throw Util.runtimeException(
// 						"Can't embed unreadable object in code: " + cs);

// 			gen.push(cs);
// 			gen.invokeStatic(RT_TYPE, readStringMethod);
// 			partial = false;
// 			}

// 		if(partial)
// 			{
// 			if(value instanceof IObj && RT.count(((IObj) value).meta()) > 0)
// 				{
// 				Object m = elideMeta(((IObj) value).meta());
// 				// Only emit meta if there is at least one value after elision.
// 				if (RT.count(m) > 0)
// 					{
// 					gen.checkCast(IOBJ_TYPE);
// 					emitValue(m, gen);
// 					gen.checkCast(IPERSISTENTMAP_TYPE);
// 					gen.invokeInterface(IOBJ_TYPE,
// 						Method.getMethod("clojure.lang.IObj withMeta(clojure.lang.IPersistentMap)"));
// 					}
// 				}
// 			}
// 	}


// 	void emitConstants(GeneratorAdapter clinitgen){
// 		boolean emitLeanCode = RT.booleanCast(EMIT_LEAN_CODE.deref());
// 		try
// 			{
// 			Var.pushThreadBindings(RT.map(RT.PRINT_DUP, RT.T));

// 			for(int i = 0; i < constants.count(); i++)
// 				if (!emitLeanCode || !(Boolean)constantLeanFlags.nth(i))
// 				{
// 				emitValue(constants.nth(i), clinitgen);
// 				clinitgen.checkCast(constantType(i));
// 				clinitgen.putStatic(objtype, constantName(i), constantType(i));
// 				}
// 			}
// 		finally
// 			{
// 			Var.popThreadBindings();
// 			}
// 	}

// 	boolean isMutable(LocalBinding lb){
// 		return isVolatile(lb) ||
// 		       RT.booleanCast(RT.contains(fields, lb.sym)) &&
// 		       RT.booleanCast(RT.get(lb.sym.meta(), Keyword.intern("unsynchronized-mutable")));
// 	}

// 	boolean isVolatile(LocalBinding lb){
// 		return RT.booleanCast(RT.contains(fields, lb.sym)) &&
// 		       RT.booleanCast(RT.get(lb.sym.meta(), Keyword.intern("volatile-mutable")));
// 	}

// 	boolean isDeftype(){
// 		return fields != null;
// 	}

// 	boolean supportsMeta(){
// 		return !isDeftype();
// 	}
// 	void emitClearCloses(GeneratorAdapter gen){
// //		int a = 1;
// //		for(ISeq s = RT.keys(closes); s != null; s = s.next(), ++a)
// //			{
// //			LocalBinding lb = (LocalBinding) s.first();
// //			Class primc = lb.getPrimitiveType();
// //			if(primc == null)
// //				{
// //				gen.loadThis();
// //				gen.visitInsn(Opcodes.ACONST_NULL);
// //				gen.putField(objtype, lb.name, OBJECT_TYPE);
// //				}
// //			}
// 	}

// 	synchronized Class getCompiledClass(){
// 		if(compiledClass == null)
// //			if(RT.booleanCast(COMPILE_FILES.deref()))
// //				compiledClass = RT.classForName(name);//loader.defineClass(name, bytecode);
// //			else
// 				{
// 				loader = (DynamicClassLoader) LOADER.deref();
// 				compiledClass = loader.defineClass(name, bytecode, src);
// 				}
// 		return compiledClass;
// 	}

	public Object eval() {
        return null;
	}

// 	public void emitLetFnInits(GeneratorAdapter gen, ObjExpr objx, IPersistentSet letFnLocals){
// 		//objx arg is enclosing objx, not this
// 		gen.checkCast(objtype);

// 		for(ISeq s = RT.keys(closes); s != null; s = s.next())
// 			{
// 			LocalBinding lb = (LocalBinding) s.first();
// 			if(letFnLocals.contains(lb))
// 				{
// 				Class primc = lb.getPrimitiveType();
// 				gen.dup();
// 				if(primc != null)
// 					{
// 					objx.emitUnboxedLocal(gen, lb);
// 					gen.putField(objtype, lb.name, Type.getType(primc));
// 					}
// 				else
// 					{
// 					objx.emitLocal(gen, lb, false);
// 					gen.putField(objtype, lb.name, OBJECT_TYPE);
// 					}
// 				}
// 			}
// 		gen.pop();

// 	}

	public void emit(C context, ObjExpr objx, GeneratorAdapter gen){
	}

	public boolean hasJavaClass() {
		return true;
	}

	public Class getJavaClass() {
		return IFn.class;
	}

// 	public void emitAssignLocal(GeneratorAdapter gen, LocalBinding lb,Expr val){
// 		if(!isMutable(lb))
// 			throw new IllegalArgumentException("Cannot assign to non-mutable: " + lb.name);
// 		Class primc = lb.getPrimitiveType();
// 		gen.loadThis();
// 		if(primc != null)
// 			{
// 			if(!(val instanceof MaybePrimitiveExpr && ((MaybePrimitiveExpr) val).canEmitPrimitive()))
// 				throw new IllegalArgumentException("Must assign primitive to primitive mutable: " + lb.name);
// 			MaybePrimitiveExpr me = (MaybePrimitiveExpr) val;
// 			me.emitUnboxed(C.EXPRESSION, this, gen);
// 			gen.putField(objtype, lb.name, Type.getType(primc));
// 			}
// 		else
// 			{
// 			val.emit(C.EXPRESSION, this, gen);
// 			gen.putField(objtype, lb.name, OBJECT_TYPE);
// 			}
// 	}

// 	private void emitLocal(GeneratorAdapter gen, LocalBinding lb, boolean clear){
// 		if(closes.containsKey(lb))
// 			{
// 			Class primc = lb.getPrimitiveType();
// 			gen.loadThis();
// 			if(primc != null)
// 				{
// 				gen.getField(objtype, lb.name, Type.getType(primc));
// 				HostExpr.emitBoxReturn(this, gen, primc);
// 				}
// 			else
// 				{
// 				gen.getField(objtype, lb.name, OBJECT_TYPE);
// 				if(onceOnly && clear && lb.canBeCleared)
// 					{
// 					gen.loadThis();
// 					gen.visitInsn(Opcodes.ACONST_NULL);
// 					gen.putField(objtype, lb.name, OBJECT_TYPE);
// 					}
// 				}
// 			}
// 		else
// 			{
// 			int argoff = isStatic?0:1;
// 			Class primc = lb.getPrimitiveType();
// //            String rep = lb.sym.name + " " + lb.toString().substring(lb.toString().lastIndexOf('@'));
// 			if(lb.isArg)
// 				{
// 				gen.loadArg(lb.idx-argoff);
// 				if(primc != null)
// 					HostExpr.emitBoxReturn(this, gen, primc);
//                 else
//                     {
//                     if(clear && lb.canBeCleared)
//                         {
// //                        System.out.println("clear: " + rep);
//                         gen.visitInsn(Opcodes.ACONST_NULL);
//                         gen.storeArg(lb.idx - argoff);
//                         }
//                     else
//                         {
// //                        System.out.println("use: " + rep);
//                         }
//                     }     
// 				}
// 			else
// 				{
// 				if(primc != null)
// 					{
// 					gen.visitVarInsn(Type.getType(primc).getOpcode(Opcodes.ILOAD), lb.idx);
// 					HostExpr.emitBoxReturn(this, gen, primc);
// 					}
// 				else
//                     {
// 					gen.visitVarInsn(OBJECT_TYPE.getOpcode(Opcodes.ILOAD), lb.idx);
//                     if(clear && lb.canBeCleared)
//                         {
// //                        System.out.println("clear: " + rep);
//                         gen.visitInsn(Opcodes.ACONST_NULL);
//                         gen.visitVarInsn(OBJECT_TYPE.getOpcode(Opcodes.ISTORE), lb.idx);
//                         }
//                     else
//                         {
// //                        System.out.println("use: " + rep);
//                         }
//                     }
// 				}
// 			}
// 	}

// 	private void emitUnboxedLocal(GeneratorAdapter gen, LocalBinding lb){
// 		int argoff = isStatic?0:1;
// 		Class primc = lb.getPrimitiveType();
// 		if(closes.containsKey(lb))
// 			{
// 			gen.loadThis();
// 			gen.getField(objtype, lb.name, Type.getType(primc));
// 			}
// 		else if(lb.isArg)
// 			gen.loadArg(lb.idx-argoff);
// 		else
// 			gen.visitVarInsn(Type.getType(primc).getOpcode(Opcodes.ILOAD), lb.idx);
// 	}

// 	public void emitVar(GeneratorAdapter gen, Var var){
//             if (RT.booleanCast(EMIT_LEAN_CODE.deref()) && isLeanVar(var)) {
//                 emitVarLean(gen, var);
//             } else {
//                 Integer i = (Integer) vars.valAt(var);
//                 emitConstant(gen, i);
//                 //gen.getStatic(fntype, munge(var.sym.toString()), VAR_TYPE);
//             }
// 	}

// 	public void emitVarLean(GeneratorAdapter gen, Var var){
//             try {
//                 // if (var.sym.name.equals("recursive-fn-with-closure"))
//                 //     throw new RuntimeException("found the fatty " + var.isNotSingleton());
//                 // if (var.objtype != null && var.objtype.equals(Type.getType(RecurExpr.class))) {
//                 //     PersistentHashMap fnTypeMap = (PersistentHashMap)FN_TYPE_MAP.deref();
//                 //     Type topFnType;
//                 //     if (fnTypeMap == null)
//                 //         topFnType = objtype;
//                 //     else {
//                 //         topFnType = objtype;
//                 //     }
//                 //     // System.out.println("Top level type: " + fnTypeMap);
//                 //     gen.getStatic(topFnType, "__instance", OBJECT_TYPE);
//                 // } else
//                 if (var.objtype == null || var.isNotSingleton()) {
//                     String typeStr = getNSClassname(var.ns);
//                     gen.getStatic(Type.getType(typeStr), munge(var.sym.name), OBJECT_TYPE);
//                 } else {
//                     gen.getStatic(var.objtype, "__instance", OBJECT_TYPE);
//                 }
//             } catch (Exception e) {
//                 throw new CompilerException((String) SOURCE_PATH.deref(), RT.intCast(LINE.deref()),
//                                             RT.intCast(COLUMN.deref()), e);
//             }
// 	}

// 	final static Method varGetMethod = Method.getMethod("Object get()");
// 	final static Method varGetRawMethod = Method.getMethod("Object getRawRoot()");

// 	public void emitVarValue(GeneratorAdapter gen, Var v){
// 		Integer i = (Integer) vars.valAt(v);
// 		if(!v.isDynamic())
// 			{
// 			emitConstant(gen, i);
// 			gen.invokeVirtual(VAR_TYPE, varGetRawMethod);
// 			}
// 		else
// 			{
// 			emitConstant(gen, i);
// 			gen.invokeVirtual(VAR_TYPE, varGetMethod);
// 			}
// 	}

// 	public void emitKeyword(GeneratorAdapter gen, Keyword k){
// 		Integer i = (Integer) keywords.valAt(k);
// 		emitConstant(gen, i);
// //		gen.getStatic(fntype, munge(k.sym.toString()), KEYWORD_TYPE);
// 	}

// 	public void emitConstant(GeneratorAdapter gen, int id){
// 		gen.getStatic(objtype, constantName(id), constantType(id));
// 	}


// 	String constantName(int id){
// 		return CONST_PREFIX + id;
// 	}

// 	String siteName(int n){
// 		return "__site__" + n;
// 	}

// 	String siteNameStatic(int n){
// 		return siteName(n) + "__";
// 	}

// 	String thunkName(int n){
// 		return "__thunk__" + n;
// 	}

// 	String cachedClassName(int n){
// 		return "__cached_class__" + n;
// 	}

// 	String cachedVarName(int n){
// 		return "__cached_var__" + n;
// 	}

// 	String varCallsiteName(int n){
// 		return "__var__callsite__" + n;
// 	}

// 	String thunkNameStatic(int n){
// 		return thunkName(n) + "__";
// 	}

// 	Type constantType(int id){
// 		Object o = constants.nth(id);
// 		Class c = clojure.lang.Util.classOf(o);
// 		if(c!= null && Modifier.isPublic(c.getModifiers()))
// 			{
// 			//can't emit derived fn types due to visibility
// 			if(LazySeq.class.isAssignableFrom(c))
// 				return Type.getType(ISeq.class);
// 			else if(c == Keyword.class)
// 				return Type.getType(Keyword.class);
// //			else if(c == KeywordCallSite.class)
// //				return Type.getType(KeywordCallSite.class);
// 			else if(RestFn.class.isAssignableFrom(c))
// 				return Type.getType(RestFn.class);
// 			else if(AFn.class.isAssignableFrom(c))
// 					return Type.getType(AFn.class);
// 				else if(c == Var.class)
// 						return Type.getType(Var.class);
// 					else if(c == String.class)
// 							return Type.getType(String.class);

// //			return Type.getType(c);
// 			}
// 		return OBJECT_TYPE;
// 	}

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

	// static class Parser implements IParser{
	// 	public Expr parse(C context, Object frm) {
	// 		ISeq form = (ISeq) frm;
	// 		//(. x fieldname-sym) or
	// 		//(. x 0-ary-method)
	// 		// (. x methodname-sym args+)
	// 		// (. x (methodname-sym args?))
	// 		if(RT.length(form) < 3)
	// 			throw new IllegalArgumentException("Malformed member expression, expecting (. target member ...)");
	// 		//determine static or instance
	// 		//static target must be symbol, either fully.qualified.Classname or Classname that has been imported
	// 		int line = lineDeref();
	// 		int column = columnDeref();
	// 		String source = (String) SOURCE.deref();
	// 		Class c = maybeClass(RT.second(form), false);
	// 		//at this point c will be non-null if static
	// 		Expr instance = null;
	// 		if(c == null)
	// 			instance = analyze(context == C.EVAL ? context : C.EXPRESSION, RT.second(form));

	// 		boolean maybeField = RT.length(form) == 3 && (RT.third(form) instanceof Symbol);

	// 		if(maybeField && !(((Symbol)RT.third(form)).name.charAt(0) == '-'))
	// 			{
	// 			Symbol sym = (Symbol) RT.third(form);
	// 			if(c != null)
	// 				maybeField = Reflector.getMethods(c, 0, munge(sym.name), true).size() == 0;
	// 			else if(instance != null && instance.hasJavaClass() && instance.getJavaClass() != null)
	// 				maybeField = Reflector.getMethods(instance.getJavaClass(), 0, munge(sym.name), false).size() == 0;
	// 			}

	// 		if(maybeField)    //field
	// 			{
	// 			Symbol sym = (((Symbol)RT.third(form)).name.charAt(0) == '-') ?
	// 				Symbol.intern(((Symbol)RT.third(form)).name.substring(1))
	// 					:(Symbol) RT.third(form);
	// 			Symbol tag = tagOf(form);
	// 			if(c != null) {
	// 				return new StaticFieldExpr(line, column, c, munge(sym.name), tag);
	// 			} else
	// 				return new InstanceFieldExpr(line, column, instance, munge(sym.name), tag, (((Symbol)RT.third(form)).name.charAt(0) == '-'));
	// 			}
	// 		else
	// 			{
	// 			ISeq call = (ISeq) ((RT.third(form) instanceof ISeq) ? RT.third(form) : RT.next(RT.next(form)));
	// 			if(!(RT.first(call) instanceof Symbol))
	// 				throw new IllegalArgumentException("Malformed member expression");
	// 			Symbol sym = (Symbol) RT.first(call);
	// 			Symbol tag = tagOf(form);
	// 			PersistentVector args = PersistentVector.EMPTY;
	// 			for(ISeq s = RT.next(call); s != null; s = s.next())
	// 				args = args.cons(analyze(context == C.EVAL ? context : C.EXPRESSION, s.first()));
	// 			if(c != null)
	// 				return new StaticMethodExpr(source, line, column, tag, c, munge(sym.name), args);
	// 			else
	// 				return new InstanceMethodExpr(source, line, column, tag, instance, munge(sym.name), args);
	// 			}
	// 	}
	// }

	// private static Class maybeClass(Object form, boolean stringOk) {
	// 	if(form instanceof Class)
	// 		return (Class) form;
	// 	Class c = null;
	// 	if(form instanceof Symbol)
	// 		{
	// 		Symbol sym = (Symbol) form;
	// 		if(sym.ns == null) //if ns-qualified can't be classname
	// 			{
	// 			if(Util.equals(sym,COMPILE_STUB_SYM.get()))
	// 				return (Class) COMPILE_STUB_CLASS.get();
	// 			if(sym.name.indexOf('.') > 0 || sym.name.charAt(0) == '[')
	// 				c = RT.classForNameNonLoading(sym.name);
	// 			else
	// 				{
	// 				Object o = currentNS().getMapping(sym);
	// 				if(o instanceof Class)
	// 					c = (Class) o;
	// 				else if(LOCAL_ENV.deref() != null && ((java.util.Map)LOCAL_ENV.deref()).containsKey(form))
	// 					return null;
	// 				else
	// 					{
	// 					try{
	// 					c = RT.classForNameNonLoading(sym.name);
	// 					}
	// 					catch(Exception e){
	// 						// aargh
	// 						// leave c set to null -> return null
	// 					}
	// 					}
	// 				}
	// 			}
	// 		}
	// 	else if(stringOk && form instanceof String)
	// 		c = RT.classForNameNonLoading((String) form);
	// 	return c;
	// }

	/*
	 private static String maybeClassName(Object form, boolean stringOk){
		 String className = null;
		 if(form instanceof Symbol)
			 {
			 Symbol sym = (Symbol) form;
			 if(sym.ns == null) //if ns-qualified can't be classname
				 {
				 if(sym.name.indexOf('.') > 0 || sym.name.charAt(0) == '[')
					 className = sym.name;
				 else
					 {
					 IPersistentMap imports = (IPersistentMap) ((Var) RT.NS_IMPORTS.get()).get();
					 className = (String) imports.valAt(sym);
					 }
				 }
			 }
		 else if(stringOk && form instanceof String)
			 className = (String) form;
		 return className;
	 }
 */
	// static Class tagToClass(Object tag) {
	// 	Class c = null;
	// 	if(tag instanceof Symbol)
	// 		{
	// 		Symbol sym = (Symbol) tag;
	// 		if(sym.ns == null) //if ns-qualified can't be classname
	// 			{
	// 			if(sym.name.equals("objects"))
	// 				c = Object[].class;
	// 			else if(sym.name.equals("ints"))
	// 				c = int[].class;
	// 			else if(sym.name.equals("longs"))
	// 				c = long[].class;
	// 			else if(sym.name.equals("floats"))
	// 				c = float[].class;
	// 			else if(sym.name.equals("doubles"))
	// 				c = double[].class;
	// 			else if(sym.name.equals("chars"))
	// 				c = char[].class;
	// 			else if(sym.name.equals("shorts"))
	// 				c = short[].class;
	// 			else if(sym.name.equals("bytes"))
	// 				c = byte[].class;
	// 			else if(sym.name.equals("booleans"))
	// 				c = boolean[].class;
	// 			else if(sym.name.equals("int"))
	// 				c = Integer.TYPE;
	// 			else if(sym.name.equals("long"))
	// 				c = Long.TYPE;
	// 			else if(sym.name.equals("float"))
	// 				c = Float.TYPE;
	// 			else if(sym.name.equals("double"))
	// 				c = Double.TYPE;
	// 			else if(sym.name.equals("char"))
	// 				c = Character.TYPE;
	// 			else if(sym.name.equals("short"))
	// 				c = Short.TYPE;
	// 			else if(sym.name.equals("byte"))
	// 				c = Byte.TYPE;
	// 			else if(sym.name.equals("boolean"))
	// 				c = Boolean.TYPE;
	// 			}
	// 		}
	// 	if(c == null)
	// 	    c = maybeClass(tag, true);
	// 	if(c != null)
	// 		return c;
	// 	throw new IllegalArgumentException("Unable to resolve classname: " + tag);
	// }
}

// static abstract class MethodExpr extends HostExpr{
// 	// static void emitArgsAsArray(IPersistentVector args, ObjExpr objx, GeneratorAdapter gen){
// 	// 	gen.push(args.count());
// 	// 	gen.newArray(OBJECT_TYPE);
// 	// 	for(int i = 0; i < args.count(); i++)
// 	// 		{
// 	// 		gen.dup();
// 	// 		gen.push(i);
// 	// 		((Expr) args.nth(i)).emit(C.EXPRESSION, objx, gen);
// 	// 		gen.arrayStore(OBJECT_TYPE);
// 	// 		}
// 	// }

// 	// public static void emitTypedArgs(ObjExpr objx, GeneratorAdapter gen, Class[] parameterTypes, IPersistentVector args){
// 	// 	for(int i = 0; i < parameterTypes.length; i++)
// 	// 		{
// 	// 		Expr e = (Expr) args.nth(i);
// 	// 		try
// 	// 			{
// 	// 			final Class primc = maybePrimitiveType(e);
// 	// 			if(primc == parameterTypes[i])
// 	// 				{
// 	// 				final MaybePrimitiveExpr pe = (MaybePrimitiveExpr) e;
// 	// 				pe.emitUnboxed(C.EXPRESSION, objx, gen);
// 	// 				}
// 	// 			else if(primc == int.class && parameterTypes[i] == long.class)
// 	// 				{
// 	// 				final MaybePrimitiveExpr pe = (MaybePrimitiveExpr) e;
// 	// 				pe.emitUnboxed(C.EXPRESSION, objx, gen);
// 	// 				gen.visitInsn(I2L);
// 	// 				}
// 	// 			else if(primc == long.class && parameterTypes[i] == int.class)
// 	// 				{
// 	// 				final MaybePrimitiveExpr pe = (MaybePrimitiveExpr) e;
// 	// 				pe.emitUnboxed(C.EXPRESSION, objx, gen);
// 	// 				if(RT.booleanCast(RT.UNCHECKED_MATH.deref()))
// 	// 					gen.invokeStatic(RT_TYPE, Method.getMethod("int uncheckedIntCast(long)"));
// 	// 				else
// 	// 					gen.invokeStatic(RT_TYPE, Method.getMethod("int intCast(long)"));
// 	// 				}
// 	// 			else if(primc == float.class && parameterTypes[i] == double.class)
// 	// 				{
// 	// 				final MaybePrimitiveExpr pe = (MaybePrimitiveExpr) e;
// 	// 				pe.emitUnboxed(C.EXPRESSION, objx, gen);
// 	// 				gen.visitInsn(F2D);
// 	// 				}
// 	// 			else if(primc == double.class && parameterTypes[i] == float.class)
// 	// 				{
// 	// 				final MaybePrimitiveExpr pe = (MaybePrimitiveExpr) e;
// 	// 				pe.emitUnboxed(C.EXPRESSION, objx, gen);
// 	// 				gen.visitInsn(D2F);
// 	// 				}
// 	// 			else
// 	// 				{
// 	// 				e.emit(C.EXPRESSION, objx, gen);
// 	// 				HostExpr.emitUnboxArg(objx, gen, parameterTypes[i]);
// 	// 				}
// 	// 			}
// 	// 		catch(Exception e1)
// 	// 			{
// 	// 			e1.printStackTrace(RT.errPrintWriter());
// 	// 			}

// 	// 		}
// 	// }
// }
 
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
