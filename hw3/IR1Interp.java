// This is supporting software for CS322 Compilers and Language Design II
// Copyright (c) Portland State University
//---------------------------------------------------------------------------
// For CS322 W'16 (J. Li).
//

// IR1 interpreter. (A starter version)
//
//
import java.util.*;
import java.io.*;
import ir.*;

public class IR1Interp {

  static class IntException extends Exception {
    public IntException(String msg) { super(msg); }
  }

  //-----------------------------------------------------------------
  // Value Representation
  //-----------------------------------------------------------------
  //
  abstract static class Val {
    //  public boolean getBool(){}
    //  public int getInt(){}
    public String toString() { return "UndVal"; }
    int getInt() throws Exception {
        throw new IntException("Int value expected");
    }
    boolean getBool() throws Exception{
        throw new IntException("Bool value expected");
    }
    static final UndVal Undefined = new UndVal();
  }

  // -- Integer values
  //
  static class IntVal extends Val {
    int i;
    IntVal(int i) { this.i = i; }
    public String toString() { return "" + i; }
    public int getInt(){ return i; }
  }

  // -- Boolean values
  //
  static class BoolVal extends Val {
    boolean b;
    BoolVal(boolean b) { this.b = b; }
    public String toString() { return "" + b; }
    public boolean getBool(){ return b; }
  }

  // -- String values
  //
  static class StrVal extends Val {
    String s;
    StrVal(String s) { this.s = s; }
    public String toString() { return s; }
  }

  // -- A special "undefined" value
  //
  static class UndVal extends Val {
  }

  //-----------------------------------------------------------------
  // Storage Organization
  //-----------------------------------------------------------------
  //

  // -- Global heap memory
  //
  static ArrayList<Val> memory;

  // -- Environment for tracking var, temp, and param's values
  //    (one copy per fuction invocation)
  //
  static class Env extends HashMap<String,Val> {
  
  }

  //-----------------------------------------------------------------
  // Other Data Structures
  //-----------------------------------------------------------------
  //
  // GUIDE:
  //  You have control over these. Either define look-up tables for 
  //  functions and labels, or searching functions.
  //

  // -- Useful global variables
  //
  static final int CONTINUE = -1;	// execution status 
  static final int RETURN = -2;		// execution status
  static Val retVal = null;             // for return value passing
  //static HashMap<String, Integer> labelMap;  // label-to-idx map
  static HashMap<String,IR1.Func> funcMap; 	// func table
  static class LabMap extends HashMap<String, Integer>{}
  static HashMap<String,LabMap> labelMap;
  
  //-----------------------------------------------------------------
  // The Main Method
  //-----------------------------------------------------------------
  //
  public static void main(String [] args) throws Exception {
    if (args.length == 1) {
      FileInputStream stream = new FileInputStream(args[0]);
      IR1.Program p = new IR1Parser(stream).Program();
      stream.close();
      IR1Interp.execute(p);
    } else {
      System.out.println("You must provide an input file name.");
    }
  }

  //-----------------------------------------------------------------
  // Top-Level Nodes
  //-----------------------------------------------------------------
  //

  // Program ---
  //  Func[] funcs;
  //
  // GUIDE:
  // 1. Establish the look-up tables (if you plan to use them).
  // 2. Look up or search for function '_main'.
  // 3. Start interpreting from '_main' with an empty Env.
  //
  public static void execute(IR1.Program n) throws Exception { 
  
    memory = new ArrayList<Val>();
    funcMap = new HashMap<String,IR1.Func>();
    labelMap = new HashMap<String, LabMap>();
    retVal = Val.Undefined;
    for (IR1.Func f: n.funcs){
      LabMap map = new LabMap();//<String, Integer>();
      int i = 0;
      for(IR1.Inst l: f.code){
        if(l instanceof IR1.LabelDec)
            map.put(((IR1.LabelDec)l).lab.name, i); 
        ++i;
      }
        funcMap.put(f.gname.s, f);
        labelMap.put(f.gname.s, map);
    }
    execute(funcMap.get("_main"), new Env());
  }

  // Func ---
  //  Global gname;
  //  Id[] params;
  //  Id[] locals;
  //  Inst[] code;
  //
  // GUIDE:
  //  - Implement the fetch-execute loop.
  //  - The parameter 'env' is the function's initial Env, which
  //    contains its parameters' values.
  //
  static void execute(IR1.Func n, Env env) throws Exception { 
    int idx = 0;
    while (idx < n.code.length) {
      int next = execute(n.code[idx], env);
      if (next == CONTINUE)
	idx++; 
      else if (next == RETURN)
        break;
      else
	idx = next;
    }
  }

  // Dispatch execution to an individual Inst node.
  //
  static int execute(IR1.Inst n, Env env) throws Exception {
    if (n instanceof IR1.Binop)    return execute((IR1.Binop) n, env);
    if (n instanceof IR1.Unop) 	   return execute((IR1.Unop) n, env);
    if (n instanceof IR1.Move) 	   return execute((IR1.Move) n, env);
    if (n instanceof IR1.Load) 	   return execute((IR1.Load) n, env);
    if (n instanceof IR1.Store)    return execute((IR1.Store) n, env);
    if (n instanceof IR1.Call)     return execute((IR1.Call) n, env);
    if (n instanceof IR1.Return)   return execute((IR1.Return) n, env);
    if (n instanceof IR1.Jump) 	   return execute((IR1.Jump) n, env);
    if (n instanceof IR1.CJump)    return execute((IR1.CJump) n, env);
    if (n instanceof IR1.LabelDec) return CONTINUE;
    throw new IntException("Unknown Inst: " + n);
  }

  //-----------------------------------------------------------------
  // Individual Instruction Nodes
  //-----------------------------------------------------------------
  //
  // - Each execute() routine returns CONTINUE, RETURN, or a new idx 
  //   (target of jump).
  //

  // Binop ---
  //  BOP op;
  //  Dest dst;
  //  Src src1, src2;
  //
  // GUIDE:
  // 1. Evaluate the operands, then perform the operation.
  // 2. Update 'dst's entry in the Env with operation's result.
  //
  static int execute(IR1.Binop n, Env env) throws Exception {
    Val src1 = evaluate(n.src1, env);
    Val src2 = evaluate(n.src2, env);
    Val res = null;
    if (src1 instanceof IntVal && src2 instanceof IntVal) 
    {
      int src1Int = src1.getInt();
      int src2Int = src2.getInt();
      if (n.op instanceof IR1.AOP) 
      {
	IR1.AOP op = (IR1.AOP) n.op;
	switch (op) 
        {
            case ADD: res = new IntVal(src1Int + src2Int); break; 
            case SUB: res = new IntVal(src1Int - src2Int); break; 
            case MUL: res = new IntVal(src1Int * src2Int); break; 
            case DIV: res = new IntVal(src1Int / src2Int); break; 
	}
      } 
      else if (n.op instanceof IR1.ROP) 
      {
	IR1.ROP op = (IR1.ROP) n.op;
	switch (op) 
        {
            case EQ: res = new BoolVal(src1Int == src2Int); break;
            case NE: res = new BoolVal(src1Int != src2Int); break;
            case LT: res = new BoolVal(src1Int < src2Int); break;
            case LE: res = new BoolVal(src1Int <= src2Int); break;
            case GT: res = new BoolVal(src1Int > src2Int); break;
            case GE: res = new BoolVal(src1Int >= src2Int); break;
	}
      }
    } 
    else if (src1 instanceof BoolVal && src2 instanceof BoolVal) 
    {
      boolean src1Bool = src1.getBool();
      boolean src2Bool = src2.getBool();
      if (n.op instanceof IR1.AOP) {
	IR1.AOP op = (IR1.AOP) n.op;
	switch (op) 
        {
            case AND: res = new BoolVal(src1Bool && src2Bool); break;
            case OR:  res = new BoolVal(src1Bool || src2Bool); break;
	}
      } 
    }
    env.put(n.dst.toString(), res);
    return CONTINUE;  
  }

  // Unop ---
  //  UOP op;
  //  Dest dst;
  //  Src src;
  //
  // GUIDE:
  // 1. Evaluate the operand, then perform the operation.
  // 2. Update 'dst's entry in the Env with operation's result.
  //
  static int execute(IR1.Unop n, Env env) throws Exception {
    Val val = evaluate(n.src, env);
    Val res = null;
    if (n.op == IR1.UOP.NEG)
      res = new IntVal(-(val.getInt()));
    else if (n.op == IR1.UOP.NOT)
      res = new BoolVal(!(val.getBool()));
    env.put(n.dst.toString(), res);
    return CONTINUE;  
  }

  // Move ---
  //  Dest dst;
  //  Src src;
  //
  // GUIDE:
  //  Evaluate 'src', then update 'dst's entry in the Env.
  //
  static int execute(IR1.Move n, Env env) throws Exception {
    Val val = evaluate(n.src, env);
    env.put(n.dst.toString(), val);
    return CONTINUE;
  }

  // Load ---  
  //  Dest dst;
  //  Addr addr;
  //
  // GUIDE:
  //  Evaluate 'addr' to a memory index, then retrieve the stored 
  //  value from memory and update 'dst's entry in the Env.
  //
  static int execute(IR1.Load n, Env env) throws Exception {
    int addr = evaluate(n.addr, env);
    Val val = memory.get(addr);
    if (val == null)
      throw new IntException("Invalid location " + addr);
    env.put(n.dst.toString(), val);
    return CONTINUE;
  }

  // Store ---  
  //  Addr addr;
  //  Src src;
  //
  // GUIDE:
  // 1. Evaluate 'src' to a value.
  // 2. Evaluate 'addr' to a memory index, then store the value
  //    to the memory entry.
  //
  static int execute(IR1.Store n, Env env) throws Exception {

    Val val = evaluate(n.src, env);
    int addr = evaluate(n.addr, env);
    memory.set(addr, val);
    return CONTINUE;
  }

  // CJump ---
  //  ROP op;
  //  Src src1, src2;
  //  Label lab;
  //
  // GUIDE:
  // 1. Evaluate the cond op.
  // 2. If cond is true, find and return the instruction index 
  //    of the jump target label; otherwise return CONTINUE.
  //
  static int execute(IR1.CJump n, Env env) throws Exception {
    boolean cond = false;
    Val src1 = evaluate(n.src1, env);
    Val src2 = evaluate(n.src2, env);
    if (src1 instanceof IntVal && src2 instanceof IntVal) 
    {
      int src1Int = src1.getInt();
      int src2Int = src2.getInt();
      switch (n.op) 
      {
          case EQ: cond = (src1Int == src2Int); break;
          case NE: cond = (src1Int != src2Int); break;
          case LT: cond = (src1Int < src2Int); break;
          case LE: cond = (src1Int <= src2Int); break;
          case GT: cond = (src1Int > src2Int); break;
          case GE: cond = (src1Int >= src2Int); break;
      }
    } 
    else if (src1 instanceof BoolVal && src2 instanceof BoolVal) 
    {
      boolean src1Bool = src1.getBool();
      boolean src2Bool = src2.getBool();
      switch (n.op) 
      {
          case EQ: cond = (src1Bool == src2Bool); break;
          case NE: cond = (src1Bool != src2Bool); break;
      }
    } 
    else {
      throw new IntException("Invalid CJump: " + n);
    }
    if (cond == true){
       for(LabMap l: labelMap.values()){
           if(l.containsKey(n.lab.name))
               return l.get(n.lab.name);
       }
       return CONTINUE;
    }
    else
      return CONTINUE;
  }	

  // Jump ---
  //  Label lab;
  //
  // GUIDE:
  //  Find and return the instruction index of the jump target label.
  //
  static int execute(IR1.Jump n, Env env) throws Exception {
       for(LabMap l: labelMap.values()){
           if(l.containsKey(n.lab.name))
               return l.get(n.lab.name);
       }
       return CONTINUE;
  }	

  // Call ---
  //  Global gname;
  //  Src[] args;
  //  Dest rdst;
  //
  // GUIDE:
  // 1. Evaluate the arguments to values.
  // 2. Create a new Env for the callee; pair function's parameter
  //    names with arguments' values, and add them to the new Env.
  // 3. Find callee's Func node and switch to execute it.
  // 4. If 'rdst' is not null, update its entry in the Env with
  //    the return value (should be avaiable in variable 'retVal').
  //
  static int execute(IR1.Call n, Env env) throws Exception {
    if (n.gname.s.equals("_printInt")) 
    { 
      if(n.args != null && n.args.length == 1)
      {
        Val val = evaluate(n.args[0], env);
        System.out.println(val);
      }
    } 
    else if (n.gname.s.equals("_printStr")) 
    {
      if (n.args == null || n.args.length == 0)
	System.out.println();
      else 
      {
	Val val = evaluate(n.args[0], env);
	System.out.println(val);
      }
    } 
    else if (n.gname.s.equals("_malloc")) 
    {
      if(n.args != null)
      {
        int size = evaluate(n.args[0], env).getInt();
        int addr = memory.size();
        for (int i=0; i<size; i++)
          memory.add(Val.Undefined);
        env.put(n.rdst.toString(), new IntVal(addr));
      }
    } 
    else 
    {
      IR1.Func func = funcMap.get(n.gname.s);
      Env newEnv = new Env();
      for (int i=0; i<func.params.length; i++) 
      {
	Val arg = evaluate(n.args[i], env);
	newEnv.put(func.params[i].toString(), arg);
      }	
      execute(func, newEnv);
      env.put(n.rdst.toString(), retVal);
    } 
    return CONTINUE;
  }	

  // Return ---  
  //  Src val;
  //
  // GUIDE:
  //  If 'val' is not null, set it to the variable 'retVal'.
  // 
  static int execute(IR1.Return n, Env env) throws Exception {

    if(n.val != null)
        retVal = evaluate(n.val, env);
    return RETURN;
  }

  //-----------------------------------------------------------------
  // Address and Operand Nodes.
  //-----------------------------------------------------------------
  //
  // - Each has an evaluate() routine.
  //

  // Address ---
  //  Src base;  
  //  int offset;
  //
  // GUIDE:
  // 1. Evaluate 'base' to an integer, then add 'offset' to it.
  // 2. Return the result (which should be an index to memory).
  //
  static int evaluate(IR1.Addr n, Env env) throws Exception {
    int addr = evaluate(n.base, env).getInt();
    return addr + n.offset;
  }

  // Src Nodes 
  //  -> Temp | Id | IntLit | BooLit | StrLit
  //
  // GUIDE:
  //  In each case, the evaluate() routine returns a Val object.
  //  - For Temp and Id, look up their value from the Env, wrap 
  //    it in a Val and return.
  //  - For the literals, wrap their value in a Val and return.
  //
  static Val evaluate(IR1.Src n, Env env) throws Exception {
    Val val = null;
     if (n instanceof IR1.Temp)    val = env.get(n.toString());
     if (n instanceof IR1.Id)      val = env.get(n.toString());
     if (n instanceof IR1.IntLit)  val = new IntVal(((IR1.IntLit)n).i);
     if (n instanceof IR1.BoolLit) val = new BoolVal(((IR1.BoolLit)n).b);
     if (n instanceof IR1.StrLit)  val = new StrVal(((IR1.StrLit)n).s);
    return val;
  }

  // Dst Nodes 
  //  -> Temp | Id
  //
  // GUIDE:
  //  For both cases, look up their value from the Env, wrap it
  //  in a Val and return.
  //
  static Val evaluate(IR1.Dest n, Env env) throws Exception {
    Val val = null;
    if(n instanceof IR1.Temp)
        val = env.get(((IR1.Temp)n).toString());    
    else if(n instanceof IR1.Id)
        val = env.get(((IR1.Id)n).toString());
    return val;
  }

}
