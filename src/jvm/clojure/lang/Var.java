/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 *   the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Jul 31, 2007 */

package clojure.lang;

import java.util.concurrent.atomic.AtomicBoolean;


public final class Var
  extends ARef
  implements IFn, IRef, Settable, Named {

  static class TBox {

    volatile Object val;
    final Thread thread;

    public TBox(Thread t, Object val) {
      this.thread = t;
      this.val = val;
    }
  }

  static public class Unbound extends AFn {
    final public Var v;

    public Unbound(Var v) {
      this.v = v;
    }

    public String toString() {
      return "Unbound: " + v;
    }

    public Object throwArity(int n) {
      throw new IllegalStateException("Attempting to call unbound fn: " + v);
    }
  }

  static class Frame {
    final static Frame TOP = new Frame(PersistentHashMap.EMPTY, null);
    //Var->TBox
    Associative bindings;
    //Var->val
//  Associative frameBindings;
    Frame prev;

    public Frame(Associative bindings, Frame prev) {
//    this.frameBindings = frameBindings;
      this.bindings = bindings;
      this.prev = prev;
    }

    protected Object clone() {
      return new Frame(this.bindings, null);
    }
  }

  static final ThreadLocal<Frame> dvals = new ThreadLocal<Frame>() {

    protected Frame initialValue() {
      return Frame.TOP;
    }
  };

  static Keyword privateKey = Keyword.intern(null, "private");
  static Keyword dynamicKey = Keyword.intern(null, "dynamic");
  static Keyword onceKey = Keyword.intern(null, "once");
  static Keyword macroKey = Keyword.intern(null, "macro");
  static Keyword nameKey = Keyword.intern(null, "name");
  static Keyword nsKey = Keyword.intern(null, "ns");

  static IPersistentMap privateMeta = new PersistentArrayMap(new Object[] {privateKey, Boolean.TRUE});

  volatile Object root;

  volatile boolean _dynamic = false;
  volatile boolean _private = false;
  volatile boolean _once = false;
  volatile boolean _macro = false;
  transient final AtomicBoolean threadBound;
  private volatile long rev = 0;
  private volatile long valrev = 0;
  public final Symbol sym;
  public final Namespace ns;

  private IPersistentMap _meta;

  public static Object getThreadBindingFrame() {
    return dvals.get();
  }

  public static Object cloneThreadBindingFrame() {
    return dvals.get().clone();
  }

  public static void resetThreadBindingFrame(Object frame) {
    dvals.set((Frame) frame);
  }

  synchronized public IPersistentMap meta() {
    return _meta;
  }

  synchronized public IPersistentMap alterMeta(IFn alter, ISeq args)  {
    _meta = alterMetaHook((IPersistentMap)alter.applyTo(new Cons(_meta, args)));
    return _meta;
  }

  synchronized public IPersistentMap resetMeta(IPersistentMap m) {
    _meta = alterMetaHook(m);
    return m;
  }

  synchronized private IPersistentMap alterMetaHook(IPersistentMap m) {
    boolean dp = RT.booleanCast(m.valAt(dynamicKey));
    if (isDynamic() && !dp)
      RT.errPrintWriter().println(
        String.format("Warning: Var %s loosing ^:dynamic %s",
                      toString(), RT.getPos()));
    this._dynamic = dp;

    boolean pp = RT.booleanCast(m.valAt(privateKey));
    if (_private && !pp)
      RT.errPrintWriter().println(
        String.format("Warning: Var %s loosing ^:private %s",
                      toString(), RT.getPos()));
    this._private = pp;

    boolean op = RT.booleanCast(m.valAt(onceKey));
    if (_once && !op)
      RT.errPrintWriter().println(
        String.format("Warning: Var %s loosing ^:once %s",
                      toString(), RT.getPos()));
    this._once = op;

    this._macro = RT.booleanCast(m.valAt(macroKey));
    return m;
  }

  public static Var intern(Namespace ns, Symbol sym, Object root) {
    return intern(ns, sym, root, true);
  }

  public static Var intern(Namespace ns, Symbol sym, Object root, boolean replaceRoot) {
    Var dvout = ns.intern(sym);
    if (!dvout.hasRoot() || replaceRoot) {
      dvout.bindRoot(root);
    }
    return dvout;
  }

  public String toString() {
    if (ns != null) {
      return "#'" + getNamespace() + "/" + getName();
    }
    return "#<Var: " + getName() + ">";
  }

  public String getName() {
    return (sym != null ? sym.toString() : "--unnamed--");
  }

  public String getNamespace() {
    if (ns != null) {
      return ns.getName();
    }
    return null;
  }

  public static Var find(Symbol nsQualifiedSym) {
    if (nsQualifiedSym.ns == null) {
      throw new IllegalArgumentException("Symbol must be namespace-qualified");
    }
    Namespace ns = Namespace.find(Symbol.intern(nsQualifiedSym.ns));
    if (ns == null) {
      throw new IllegalArgumentException("No such namespace: " + nsQualifiedSym.ns);
    }
    return ns.findInternedVar(Symbol.intern(nsQualifiedSym.name));
  }

  public static Var intern(Symbol nsName, Symbol sym) {
    Namespace ns = Namespace.findOrCreate(nsName);
    return intern(ns, sym);
  }

  public static Var internPrivate(String nsName, String sym) {
    Namespace ns = Namespace.findOrCreate(Symbol.intern(nsName));
    Var ret = intern(ns, Symbol.intern(sym));
    ret.setMeta(privateMeta);
    return ret;
  }

  public static Var intern(Namespace ns, Symbol sym) {
    return ns.intern(sym);
  }

  public static Var create() {
    return new Var(null, null);
  }

  public static Var create(Object root) {
    return new Var(null, null, root);
  }

  Var(Namespace ns, Symbol sym) {
    this.ns = ns;
    this.sym = sym;
    this.threadBound = new AtomicBoolean(false);
    this.root = new Unbound(this);
    this.rev = nsRev();
    setMeta(PersistentHashMap.EMPTY);
  }

  Var(Namespace ns, Symbol sym, Object root) {
    this(ns, sym);
    this.root = root;
    ++rev;
  }

  public boolean isBound() {
    return hasRoot() || (threadBound.get() && dvals.get().bindings.containsKey(this));
  }

  final public Object get() {
    if (!threadBound.get()) {
      return root;
    }
    return deref();
  }

  private long nsRev() {
    long v = ns == null ? -1 : ns.getRev();
    return v;
  }

  public synchronized long getRev() {
    return rev;
  }

  public synchronized void resetRev() {
    rev = nsRev();
  }

  public boolean isStale() {
    return !isOnce() && rev < nsRev();
  }

  final public Object deref() {
    TBox b = getThreadBinding();
    if (b != null) {
      return b.val;
    }
    return root;
  }

  public void setValidator(IFn vf) {
    if (hasRoot()) {
      validate(vf, root);
    }
    validator = vf;
  }

  public Object alter(IFn fn, ISeq args) {
    set(fn.applyTo(RT.cons(deref(), args)));
    return this;
  }

  public Object set(Object val) {
    validate(getValidator(), val);
    TBox b = getThreadBinding();
    if (b != null) {
      if (Thread.currentThread() != b.thread) {
        throw new IllegalStateException(String.format("Can't set!: %s from non-binding thread", sym));
      }
      return (b.val = val);
    }
    throw new IllegalStateException(String.format("Can't change/establish root binding of: %s with set", sym));
  }

  public Object doSet(Object val)  {
    return set(val);
  }

  public Object doReset(Object val)  {
    bindRoot(val);
    return val;
  }

  public void setMeta(IPersistentMap m) {
    //ensure these basis keys
    resetMeta(m.assoc(nameKey, sym).assoc(nsKey, ns));
  }

  public Var setDynamic() {
    return setDynamic(true);
  }

  public Var setDynamic(boolean b) {
    resetMeta(meta().assoc(dynamicKey, b));
    return this;
  }

  public final boolean isDynamic() {
    return _dynamic;
  }

  public Var setMacro() {
    return setMacro(true);
  }

  private Var setMacro(boolean b) {
    resetMeta(meta().assoc(macroKey, b));
    return this;
  }

  public boolean isMacro() {
    return _macro;
  }

  public Var setPublic() {
    return setPublic(true);
  }

  public Var setPublic(boolean b) {
    resetMeta(meta().assoc(privateKey, !b));
    return this;
  }

  public boolean isPublic() {
    return !_private;
  }

  public Var setOnce() {
    return setOnce(true);
  }

  public Var setOnce(boolean state) {
    resetMeta(meta().assoc(onceKey, state));
    return this;
  }

  public boolean isOnce() {
    return _once;
  }

  final public Object getRawRoot() {
    return root;
  }

  public Object getTag() {
    return meta().valAt(RT.TAG_KEY);
  }

  public void setTag(Symbol tag) {
    alterMeta(assoc, RT.list(RT.TAG_KEY, tag));
  }

  final public boolean hasRoot() {
    return !(root instanceof Unbound);
  }

  //binding root always clears macro flag
  synchronized public void bindRoot(Object root) {
    validate(getValidator(), root);
    Object oldroot = this.root;
    this.root = root;
    ++valrev;
    resetRev();
    setMacro(false);
    notifyWatches(oldroot,this.root);
  }

  synchronized void swapRoot(Object root) {
    validate(getValidator(), root);
    Object oldroot = this.root;
    this.root = root;
    ++valrev;
    resetRev();
    notifyWatches(oldroot,root);
  }

  synchronized public void unbindRoot() {
    this.root = new Unbound(this);
    ++valrev;
    rev = -1;
  }

  synchronized public void commuteRoot(IFn fn) {
    Object newRoot = fn.invoke(root);
    validate(getValidator(), newRoot);
    Object oldroot = root;
    this.root = newRoot;
    ++valrev;
    resetRev();
    notifyWatches(oldroot,newRoot);
  }

  synchronized public Object alterRoot(IFn fn, ISeq args) {
    Object newRoot = fn.applyTo(RT.cons(root, args));
    validate(getValidator(), newRoot);
    Object oldroot = root;
    this.root = newRoot;
    ++valrev;
    resetRev();
    notifyWatches(oldroot,newRoot);
    return newRoot;
  }

  public static void pushThreadBindings(Associative bindings) {
    Frame f = dvals.get();
    Associative bmap = f.bindings;
    for (ISeq bs = bindings.seq(); bs != null; bs = bs.next()) {
      IMapEntry e = (IMapEntry) bs.first();
      Var v = (Var) e.key();
      if (!v.isDynamic()) {
        throw new IllegalStateException(String.format("Can't dynamically bind non-dynamic var: %s/%s", v.ns, v.sym));
      }
      v.validate(v.getValidator(), e.val());
      v.threadBound.set(true);
      bmap = bmap.assoc(v, new TBox(Thread.currentThread(), e.val()));
    }
    dvals.set(new Frame(bmap, f));
  }

  public static void popThreadBindings() {
    Frame f = dvals.get().prev;
    if (f == null) {
      throw new IllegalStateException("Pop without matching push");
    } else if (f == Frame.TOP) {
      dvals.remove();
    } else {
      dvals.set(f);
    }
  }

  public static Associative getThreadBindings() {
    Frame f = dvals.get();
    IPersistentMap ret = PersistentHashMap.EMPTY;
    for (ISeq bs = f.bindings.seq(); bs != null; bs = bs.next()) {
      IMapEntry e = (IMapEntry) bs.first();
      Var v = (Var) e.key();
      TBox b = (TBox) e.val();
      ret = ret.assoc(v, b.val);
    }
    return ret;
  }

  public final TBox getThreadBinding() {
    if (threadBound.get()) {
      IMapEntry e = dvals.get().bindings.entryAt(this);
      if (e != null) {
        return (TBox) e.val();
      }
    }
    return null;
  }

  final public IFn fn() {
    return (IFn) deref();
  }

  public Object call() {
    return invoke();
  }

  public void run() {
    invoke();
  }

  public Object invoke() {
    return fn().invoke();
  }

  public Object invoke(Object arg1) {
    return fn().invoke(Util.ret1(arg1,arg1=null));
  }

  public Object invoke(Object arg1, Object arg2) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11, Object arg12) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null),
                       Util.ret1(arg12,arg12=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null),
                       Util.ret1(arg12,arg12=null),
                       Util.ret1(arg13,arg13=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null),
                       Util.ret1(arg12,arg12=null),
                       Util.ret1(arg13,arg13=null),
                       Util.ret1(arg14,arg14=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14,
                       Object arg15) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null),
                       Util.ret1(arg12,arg12=null),
                       Util.ret1(arg13,arg13=null),
                       Util.ret1(arg14,arg14=null),
                       Util.ret1(arg15,arg15=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14,
                       Object arg15, Object arg16) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null),
                       Util.ret1(arg12,arg12=null),
                       Util.ret1(arg13,arg13=null),
                       Util.ret1(arg14,arg14=null),
                       Util.ret1(arg15,arg15=null),
                       Util.ret1(arg16,arg16=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14,
                       Object arg15, Object arg16, Object arg17) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null),
                       Util.ret1(arg12,arg12=null),
                       Util.ret1(arg13,arg13=null),
                       Util.ret1(arg14,arg14=null),
                       Util.ret1(arg15,arg15=null),
                       Util.ret1(arg16,arg16=null),
                       Util.ret1(arg17,arg17=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14,
                       Object arg15, Object arg16, Object arg17, Object arg18) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null),
                       Util.ret1(arg12,arg12=null),
                       Util.ret1(arg13,arg13=null),
                       Util.ret1(arg14,arg14=null),
                       Util.ret1(arg15,arg15=null),
                       Util.ret1(arg16,arg16=null),
                       Util.ret1(arg17,arg17=null),
                       Util.ret1(arg18,arg18=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14,
                       Object arg15, Object arg16, Object arg17, Object arg18, Object arg19) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null),
                       Util.ret1(arg12,arg12=null),
                       Util.ret1(arg13,arg13=null),
                       Util.ret1(arg14,arg14=null),
                       Util.ret1(arg15,arg15=null),
                       Util.ret1(arg16,arg16=null),
                       Util.ret1(arg17,arg17=null),
                       Util.ret1(arg18,arg18=null),
                       Util.ret1(arg19,arg19=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14,
                       Object arg15, Object arg16, Object arg17, Object arg18, Object arg19, Object arg20) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null),
                       Util.ret1(arg12,arg12=null),
                       Util.ret1(arg13,arg13=null),
                       Util.ret1(arg14,arg14=null),
                       Util.ret1(arg15,arg15=null),
                       Util.ret1(arg16,arg16=null),
                       Util.ret1(arg17,arg17=null),
                       Util.ret1(arg18,arg18=null),
                       Util.ret1(arg19,arg19=null),
                       Util.ret1(arg20,arg20=null));
  }

  public Object invoke(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6, Object arg7,
                       Object arg8, Object arg9, Object arg10, Object arg11, Object arg12, Object arg13, Object arg14,
                       Object arg15, Object arg16, Object arg17, Object arg18, Object arg19, Object arg20,
                       Object... args) {
    return fn().invoke(Util.ret1(arg1,arg1=null),
                       Util.ret1(arg2,arg2=null),
                       Util.ret1(arg3,arg3=null),
                       Util.ret1(arg4,arg4=null),
                       Util.ret1(arg5,arg5=null),
                       Util.ret1(arg6,arg6=null),
                       Util.ret1(arg7,arg7=null),
                       Util.ret1(arg8,arg8=null),
                       Util.ret1(arg9,arg9=null),
                       Util.ret1(arg10,arg10=null),
                       Util.ret1(arg11,arg11=null),
                       Util.ret1(arg12,arg12=null),
                       Util.ret1(arg13,arg13=null),
                       Util.ret1(arg14,arg14=null),
                       Util.ret1(arg15,arg15=null),
                       Util.ret1(arg16,arg16=null),
                       Util.ret1(arg17,arg17=null),
                       Util.ret1(arg18,arg18=null),
                       Util.ret1(arg19,arg19=null),
                       Util.ret1(arg20,arg20=null),
                       (Object[])Util.ret1(args, args=null));
  }

  public Object applyTo(ISeq arglist) {
    return AFn.applyToHelper(this, arglist);
  }

  static IFn assoc = new AFn() {
    @Override
    public Object invoke(Object m, Object k, Object v)  {
      return RT.assoc(m, k, v);
    }
  };
  static IFn dissoc = new AFn() {
    @Override
    public Object invoke(Object c, Object k)  {
      return RT.dissoc(c, k);
    }
  };
}
