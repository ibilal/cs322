// This is supporting software for CS321/CS322 Compilers and Language Design.
// Copyright (c) Portland State University
//---------------------------------------------------------------------------
// For CS322 W'16 (J. Li).
//

// IR1 code generator.
//
import java.util.*;
import java.io.*;
import ast.*;
import ir.*;

class IR1Gen {

  static class GenException extends Exception {
    public GenException(String msg) { super(msg); }
  }

  // For returning <src,code> pair from gen routines
  //
  static class CodePack {
    IR1.Src src;
    List<IR1.Inst> code;
    CodePack(IR1.Src src, List<IR1.Inst> code) { 
      this.src=src; this.code=code; 
    }
  } 

  // The main routine
  //
  public static void main(String [] args) throws Exception {
    if (args.length == 1) {
      FileInputStream stream = new FileInputStream(args[0]);
      Ast1.Program p = new Ast1Parser(stream).Program();
      stream.close();
      IR1.Program ir = IR1Gen.gen(p);
      System.out.print(ir.toString());
    } else {
      System.out.println("You must provide an input file name.");
    }
  }

  // Ast1.Program  ---
  //
  //
  public static IR1.Program gen(Ast1.Program n) throws Exception {
    List<IR1.Func> code = new ArrayList<IR1.Func>();
    for (Ast1.Func f: n.funcs){
        code.add(gen(f));
    }
    IR1.Program program = new IR1.Program(code);
    return new IR1.Program(code);
    
  }
  // Ast1.Func ---
  // Ast1.Type t;
  // String nm
  // Ast.Param[] params;
  // Ast.VarDecl[] vars;
  // Ast1.Stmt[] stmts;
  //
  //
  public static IR1.Func gen(Ast1.Func n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    List<IR1.Id> params = new ArrayList<IR1.Id>();
    List<IR1.Id> locals = new ArrayList<IR1.Id>();
    for (Ast1.Param m: n.params){
        params.add(new IR1.Id(m.nm));
    }
    for (Ast1.VarDecl m: n.vars){
        IR1.Id toAdd = new IR1.Id(m.nm);
        locals.add(toAdd);
        if(m.init != null)
            code.addAll(gen(m)); ///new IR1.Move(toAdd, var.src));
    }
    for (Ast1.Stmt m: n.stmts)
        code.addAll(gen(m));

    if(n.t == null)
        code.add(new IR1.Return());
    return new IR1.Func(new IR1.Global("_" + n.nm), params, locals, code);
  }

  // Ast1.VarDecl ---
  // Ast1.Type t;
  // String nm;
  // Ast1.Exp init;
  //
  // AG:
  //   NEED TO GET THIS FROM PDF
  //
  static List<IR1.Inst> gen(Ast1.VarDecl n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    IR1.Id toAdd = new IR1.Id(n.nm);
    CodePack var = gen(n.init);
    code.addAll(var.code);
    code.add(new IR1.Move(toAdd, var.src));
    return code;
  
  }
  // Ast1.Param ----
  // Ast1.Type t;
  // String nm;
  //
  // AG:
  //   NEED TO GET THIS FROM PDF
  //
  static IR1.Id gen(Ast1.Param n) throws Exception {

    return new IR1.Id(n.nm);
  
  }

  // IR1.Addr
  //
  static CodePack genAddr(Ast1.ArrayElm n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    IR1.Temp temp1 = new IR1.Temp();
    IR1.Temp temp2 = new IR1.Temp();

    CodePack arExp = gen(n.ar);
    CodePack idxExp = gen(n.idx);
    code.addAll(arExp.code);
    code.addAll(idxExp.code);

    code.add(new IR1.Binop(IR1.AOP.MUL, temp1, idxExp.src, new IR1.IntLit(4)));
    code.add(new IR1.Binop(IR1.AOP.ADD, temp2, arExp.src, temp1));
    
    CodePack toReturn = new CodePack(temp2, code);
    return toReturn;
  
  }

  // STATEMENTS

  static List<IR1.Inst> gen(Ast1.Stmt n) throws Exception {
    if (n instanceof Ast1.Return) return gen((Ast1.Return) n);
    if (n instanceof Ast1.Assign) return gen((Ast1.Assign) n);
    if (n instanceof Ast1.CallStmt) return gen((Ast1.CallStmt) n);
    if (n instanceof Ast1.Block)  return gen((Ast1.Block) n);
    if (n instanceof Ast1.If)     return gen((Ast1.If) n);
    if (n instanceof Ast1.While)  return gen((Ast1.While) n);
    if (n instanceof Ast1.Print)  return gen((Ast1.Print) n);
    throw new GenException("Unknown Stmt: " + n);
  }

  // Ast1.Return
  // Exp val;
  //
  static List<IR1.Inst> gen(Ast1.Return n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    CodePack valExp = gen(n.val);
    code.addAll(valExp.code);
    code.add(new IR1.Return(valExp.src));
    return code;
  }

  // Ast1.Assign ---
  // Ast1.Id lhs;
  // Ast1.Exp rhs;
  //
  // AG:
  //   code: rhs.c + lhs.c + "lhs.s = rhs.v"
  //
  static List<IR1.Inst> gen(Ast1.Assign n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
   
    CodePack rhsExpression = gen(n.rhs);
    CodePack lhsExpression = gen(n.lhs);
    code.addAll(rhsExpression.code);
    code.addAll(lhsExpression.code);
    if(n.lhs instanceof Ast1.Id){
        code.add(new IR1.Move((IR1.Id)lhsExpression.src, rhsExpression.src));
    }
    else if(n.lhs instanceof Ast1.ArrayElm){
        CodePack arrayElement = genAddr((Ast1.ArrayElm) n.lhs);
        code.addAll(arrayElement.code);
        code.add(new IR1.Store(new IR1.Addr(arrayElement.src), rhsExpression.src));
    }

    return code;
  }



  // Ast1.CallStmt
  // String nm
  // Exp [] args
  //
  static List<IR1.Inst> gen(Ast1.CallStmt n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    List<IR1.Src> srcs = new ArrayList<IR1.Src>();
    for(Ast1.Exp e: n.args)
    {
        CodePack toAdd = gen(e);
        srcs.add(toAdd.src);
        code.addAll(toAdd.code);
    }
    code.add(new IR1.Call(new IR1.Global("_" + n.nm), srcs)); 
    return code;
  }


  //
  // Ast1.Block ---
  // Ast1.Stmt[] stmts;
  //
  // AG:
  //   code: {stmt.c}
  //
  static List<IR1.Inst> gen(Ast1.Block n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();

    for (Ast1.Stmt s: n.stmts){
      code.addAll(gen(s));
    }
    return code;

  }
  
  
  // Ast1.If ---
  // Ast1.Exp cond;
  // Ast1.Stmt s1, s2;
  //
  // AG:
  //   newLabel: L1[,L2]
  //   code: cond.c 
  //         + "if cond.v == false goto L1" 
  //         + s1.c 
  //         [+ "goto L2"] 
  //         + "L1:" 
  //         [+ s2.c]
  //         [+ "L2:"]
  //
  static List<IR1.Inst> gen(Ast1.If n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    IR1.Label label1 = new IR1.Label();
    IR1.Label label2 = null;
    boolean s2Flag = false;

    if(n.s2 != null){  //determine if s2 is null
        label2 = new IR1.Label();
        s2Flag = true;
    }
    CodePack condExpr = gen(n.cond);  //generate codepack for cond
    code.addAll(condExpr.code);     //add all code from cond codepack
    IR1.CJump inst = new IR1.CJump(IR1.ROP.EQ, condExpr.src, IR1.FALSE, label1); 
    code.add(inst);
    code.addAll(gen(n.s1));
    
    if(s2Flag)
        code.add(new IR1.Jump(label2));
    code.add(new IR1.LabelDec(label1));
    if(s2Flag){
        code.addAll(gen(n.s2));
        code.add(new IR1.LabelDec(label2));
    }
    return code;
  }

  // Ast1.While ---
  // Ast1.Exp cond;
  // Ast1.Stmt s;
  //
  // AG:
  //   newLabel: L1,L2
  //   code: "L1:" 
  //         + cond.c 
  //         + "if cond.v == false goto L2" 
  //         + s.c 
  //         + "goto L1" 
  //         + "L2:"
  //
  static List<IR1.Inst> gen(Ast1.While n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    IR1.Label label1 = new IR1.Label();
    IR1.Label label2 = new IR1.Label();
    boolean s2Flag = false;
    code.add(new IR1.LabelDec(label1));
    CodePack condExpr = gen(n.cond);
    code.addAll(condExpr.code);
    code.add(new IR1.CJump(IR1.ROP.EQ, condExpr.src, IR1.FALSE, label2));
    code.addAll(gen(n.s));
    code.add(new IR1.Jump(label1));
    code.add(new IR1.LabelDec(label2));


    return code;
  }
  
  // Ast1.Print ---
  // Ast1.Exp arg;
  //
  // AG:
  //   code: arg.c + "print (arg.v)"
  //
  static List<IR1.Inst> gen(Ast1.Print n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    List<IR1.Src> srcs = new ArrayList<IR1.Src>();
//    if(n.arg != null){
        CodePack argExpr = gen(n.arg);
        code.addAll(argExpr.code);
        srcs.add(argExpr.src);
        if(argExpr.src instanceof IR1.StrLit || n.arg == null){
            IR1.Call call = new IR1.Call(new IR1.Global("_printStr"), srcs);
           code.add(call);
        }
        else{
           IR1.Call call = new IR1.Call(new IR1.Global("_printInt"), srcs); 
           code.add(call);
        }
    return code;
  }

  // EXPRESSIONS

  static CodePack gen(Ast1.Exp n) throws Exception {
    if (n instanceof Ast1.Call) return gen((Ast1.Call) n);
    if (n instanceof Ast1.Binop)    return gen((Ast1.Binop) n);
    if (n instanceof Ast1.NewArray) return gen((Ast1.NewArray) n);
    if (n instanceof Ast1.ArrayElm) return gen((Ast1.ArrayElm) n);
    if (n instanceof Ast1.Unop)     return gen((Ast1.Unop) n);
    if (n instanceof Ast1.Id)       return gen((Ast1.Id) n);
    if (n instanceof Ast1.IntLit)   return gen((Ast1.IntLit) n);
    if (n instanceof Ast1.BoolLit)  return gen((Ast1.BoolLit) n);
    if (n instanceof Ast1.StrLit) return gen((Ast1.StrLit) n);
    throw new GenException("Unknown Exp node: " + n);
  }

  // Ast1.Call ---
  // String nm;
  // Exp[] args;
  //
  //
  static CodePack gen(Ast1.Call n) throws Exception{
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    List<IR1.Src> srcs = new ArrayList<IR1.Src>();
    IR1.Temp atemp = new IR1.Temp();

    for(Ast1.Exp e: n.args)
    {
        CodePack toAdd = gen(e);
        srcs.add(toAdd.src);
        code.addAll(toAdd.code);
    }
    code.add(new IR1.Call(new IR1.Global("_" + n.nm), srcs, atemp)); 
    return new CodePack(atemp, code);
  
  }
  // Ast1.NewArray ----
  // Type et;
  // int len;
  //
  //
  static CodePack gen(Ast1.NewArray n) throws Exception{
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    IR1.Temp temp1 = new IR1.Temp();
    IR1.Temp temp2 = new IR1.Temp();
    List<IR1.Src> srcs = new ArrayList<IR1.Src>();
    code.add(new IR1.Binop(IR1.AOP.MUL, temp1, new IR1.IntLit(n.len), new IR1.IntLit(4)));
    srcs.add(temp1);
    code.add(new IR1.Call(new IR1.Global("_malloc"), srcs, temp2));

    return new CodePack(temp2, code);
  }

  // Ast1.ArrayElm ----
  // Exp ar;
  // Exp idx;
  //
  static CodePack gen(Ast1.ArrayElm n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    IR1.Temp temp1 = new IR1.Temp();
    IR1.Temp temp2 = new IR1.Temp();
    IR1.Temp temp3 = new IR1.Temp();

    CodePack arExp = gen(n.ar);
    CodePack idxExp = gen(n.idx);
    code.addAll(arExp.code);
    code.addAll(idxExp.code);

    code.add(new IR1.Binop(IR1.AOP.MUL, temp1, idxExp.src, new IR1.IntLit(4)));
    code.add(new IR1.Binop(IR1.AOP.ADD, temp2, arExp.src, temp1));
    code.add(new IR1.Load(temp3, new IR1.Addr(temp2)));
    
    return new CodePack(temp3, code);
  }

  // Ast1.Binop ----
  // Ast1.BOP op;
  // Ast1.Exp e1,e2;
  //
  // AG:
  //   newTemp: t
  //   code: e1.c + e2.c
  //         + "t = e1.v op e2.v"
  //
  static CodePack gen(Ast1.Binop n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    IR1.Temp atemp = new IR1.Temp();
    CodePack e1Expr = gen(n.e1);
    CodePack e2Expr = gen(n.e2);
    code.addAll(e1Expr.code);
    code.addAll(e2Expr.code);
    IR1.BOP op = gen(n.op);
/*    if(e1Expr.src instanceof IR1.IntLit && e2Expr.src instanceof IR1.IntLit)
    {
        int e1 = 0;
        int e2 = 0;
        try{
            e1 = Integer.parseInt(e1Expr.src.toString());
            e2 = Integer.parseInt(e2Expr.src.toString());
        }
        catch(Exception e){
            throw new NumberFormatException
        }

        if(op == IR1.AOP.ADD)
                return new CodePack(new IR1.IntLit(e1 + e2), code); 
        if(op == IR1.AOP.SUB)
                return new CodePack(new IR1.IntLit(Integer.parseInt(e1Expr.toString()) - Integer.parseInt(e2Expr.toString())), code); 
        if(op == IR1.AOP.MUL)
                return new CodePack(new IR1.IntLit(Integer.parseInt(e1Expr.toString()) * Integer.parseInt(e2Expr.toString())), code); 
        if(op == IR1.AOP.DIV)
                return new CodePack(new IR1.IntLit(Integer.parseInt(e1Expr.toString()) / Integer.parseInt(e2Expr.toString())), code); 
        if(op == IR1.ROP.EQ){ 
        }
        if(op == IR1.ROP.NE){}
        if(op == IR1.ROP.LT){}
        if(op == IR1.ROP.LE){}
        if(op == IR1.ROP.GT){}
        if(op == IR1.ROP.GE){
        }

    }*/
    code.add(new IR1.Binop(op, atemp, e1Expr.src, e2Expr.src));

    
    return new CodePack(atemp, code);
  }

  // Ast1.Unop ---
  // Ast1.UOP op;
  // Ast1.Exp e;
  //
  // AG:
  //   newTemp: t
  //   code: e.c + "t = op e.v"
  //
  static CodePack gen(Ast1.Unop n) throws Exception {
    List<IR1.Inst> code = new ArrayList<IR1.Inst>();
    IR1.Temp atemp = new IR1.Temp();
    IR1.UOP op = null;
    CodePack eExpr = gen(n.e);
    code.addAll(eExpr.code);
    Ast1.UOP neg = Ast1.UOP.NEG;
    Ast1.UOP not = Ast1.UOP.NOT;
    if(n.op == neg){
        op = IR1.UOP.NEG;
        code.add(new IR1.Unop(op, atemp, eExpr.src));
    }
    else if(n.op == not){
        op = IR1.UOP.NOT;
        code.add(new IR1.Unop(op, atemp, eExpr.src));
    }

    return new CodePack(atemp, code);

  }
  
  // Ast1.Id ---
  // String nm;
  //
  static CodePack gen(Ast1.Id n) throws Exception {

    return new CodePack(new IR1.Id(n.nm), new ArrayList<IR1.Inst>());

  }

  // Ast1.StrLit ----
  // String s;
  //
  static CodePack gen(Ast1.StrLit n) throws Exception {

    return new CodePack(new IR1.StrLit(n.s), new ArrayList<IR1.Inst>());
  }


  // Ast1.IntLit ---
  // int i;
  //
  static CodePack gen(Ast1.IntLit n) throws Exception {

    return new CodePack(new IR1.IntLit(n.i), new ArrayList<IR1.Inst>());

  }

  // Ast1.BoolLit ---
  // boolean b;
  //
  static CodePack gen(Ast1.BoolLit n) throws Exception {

    return new CodePack(new IR1.BoolLit(n.b), new ArrayList<IR1.Inst>());

  }

  // OPERATORS

  static IR1.BOP gen(Ast1.BOP op) {
    IR1.BOP irOp = null;
    switch (op) {
    case ADD: irOp = IR1.AOP.ADD; break;
    case SUB: irOp = IR1.AOP.SUB; break;
    case MUL: irOp = IR1.AOP.MUL; break;
    case DIV: irOp = IR1.AOP.DIV; break;
    case AND: irOp = IR1.AOP.AND; break;
    case OR:  irOp = IR1.AOP.OR;  break;
    case EQ:  irOp = IR1.ROP.EQ;  break;
    case NE:  irOp = IR1.ROP.NE;  break;
    case LT:  irOp = IR1.ROP.LT;  break;
    case LE:  irOp = IR1.ROP.LE;  break;
    case GT:  irOp = IR1.ROP.GT;  break;
    case GE:  irOp = IR1.ROP.GE;  break;
    }
    return irOp;
  }
}

