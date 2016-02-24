// This is supporting software for CS321/CS322 Compilers and Language Design.
// Copyright (c) Portland State University
//---------------------------------------------------------------------------
// For CS322 W'16 (J. Li).
//

// IR0 code generator.
//
//
import java.util.*;
import java.io.*;
import ast.*;
import ir.*;

class IR0Gen {

  static class GenException extends Exception {
    public GenException(String msg) { super(msg); }
  }

  // For returning <src,code> pair from gen routines
  //
  static class CodePack {
    IR0.Src src;
    List<IR0.Inst> code;
    CodePack(IR0.Src src, List<IR0.Inst> code) { 
      this.src=src; this.code=code; 
    }
  } 

  // The main routine
  //
  public static void main(String [] args) throws Exception {
    if (args.length == 1) {
      FileInputStream stream = new FileInputStream(args[0]);
      Ast0.Program p = new Ast0Parser(stream).Program();
      stream.close();
      IR0.Program ir = IR0Gen.gen(p);
      System.out.print(ir.toString());
    } else {
      System.out.println("You must provide an input file name.");
    }
  }

  // Ast0.Program ---
  // Ast0.Stmt[] stmts;
  //
  // AG:
  //   code: stmts.c  -- append all individual stmt.c
  //
  public static IR0.Program gen(Ast0.Program n) throws Exception {
    List<IR0.Inst> code = new ArrayList<IR0.Inst>();
    for (Ast0.Stmt s: n.stmts)
      code.addAll(gen(s));
    return new IR0.Program(code);
  }

  // STATEMENTS

  static List<IR0.Inst> gen(Ast0.Stmt n) throws Exception {
    if (n instanceof Ast0.Block)  return gen((Ast0.Block) n);
    if (n instanceof Ast0.Assign) return gen((Ast0.Assign) n);
    if (n instanceof Ast0.If)     return gen((Ast0.If) n);
    if (n instanceof Ast0.While)  return gen((Ast0.While) n);
    if (n instanceof Ast0.Print)  return gen((Ast0.Print) n);
    throw new GenException("Unknown Stmt: " + n);
  }

  // Ast0.Block ---
  // Ast0.Stmt[] stmts;
  //
  // AG:
  //   code: {stmt.c}
  //
  static List<IR0.Inst> gen(Ast0.Block n) throws Exception {
    List<IR0.Inst> code = new ArrayList<IR0.Inst>();

    for (Ast0.Stmt s: n.stmts){
      code.addAll(gen(s));
    }
    return code;

    // ... need code ...

  }

  // Ast0.Assign ---
  // Ast0.Id lhs;
  // Ast0.Exp rhs;
  //
  // AG:
  //   code: rhs.c + "lhs.s = rhs.v"
  //
  static List<IR0.Inst> gen(Ast0.Assign n) throws Exception {
    List<IR0.Inst> code = new ArrayList<IR0.Inst>();
    CodePack rhsExpression = gen(n.rhs);
    CodePack lhsExpression = gen(n.lhs);
    code.addAll(rhsExpression.code);
    code.addAll(lhsExpression.code);
    code.add(new IR0.Move(new IR0.Id(n.lhs.nm), rhsExpression.src)); 

    // ... need code ...

    return code;
  }

  // Ast0.If ---
  // Ast0.Exp cond;
  // Ast0.Stmt s1, s2;
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
  static List<IR0.Inst> gen(Ast0.If n) throws Exception {
    List<IR0.Inst> code = new ArrayList<IR0.Inst>();
    IR0.Label label1 = new IR0.Label();
    IR0.Label label2 = null;
    boolean s2Flag = false;

    if(n.s2 != null){  //determine if s2 is null
        label2 = new IR0.Label();
        s2Flag = true;
    }
    CodePack condExpr = gen(n.cond);  //generate codepack for cond
    code.addAll(condExpr.code);     //add all code from cond codepack
    IR0.CJump inst = new IR0.CJump(IR0.ROP.EQ, condExpr.src, new IR0.BoolLit(false), label1); 
    code.add(inst);
    code.addAll(gen(n.s1));
    
    if(s2Flag)
        code.add(new IR0.Jump(label2));
    code.add(new IR0.LabelDec(label1));
    if(s2Flag){
        code.addAll(gen(n.s2));
        code.add(new IR0.LabelDec(label2));
    }

    // ... need code ...

    return code;
  }

  // Ast0.While ---
  // Ast0.Exp cond;
  // Ast0.Stmt s;
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
  static List<IR0.Inst> gen(Ast0.While n) throws Exception {
    List<IR0.Inst> code = new ArrayList<IR0.Inst>();

    IR0.Label label1 = new IR0.Label();
    IR0.Label label2 = new IR0.Label();
    boolean s2Flag = false;
    code.add(new IR0.LabelDec(label1));
    CodePack condExpr = gen(n.cond);
    code.addAll(condExpr.code);
    code.add(new IR0.CJump(IR0.ROP.EQ, condExpr.src, new IR0.BoolLit(false), label2));
    code.addAll(gen(n.s));
    code.add(new IR0.Jump(label1));
    code.add(new IR0.LabelDec(label2));
    // ... need code ...

    return code;
  }
  
  // Ast0.Print ---
  // Ast0.Exp arg;
  //
  // AG:
  //   code: arg.c + "print (arg.v)"
  //
  static List<IR0.Inst> gen(Ast0.Print n) throws Exception {
    List<IR0.Inst> code = new ArrayList<IR0.Inst>();

    CodePack argExpr = gen(n.arg);
    code.addAll(argExpr.code);
    code.add(new IR0.Print(argExpr.src));
    // ... need code ...

    return code;
  }

  // EXPRESSIONS

  static CodePack gen(Ast0.Exp n) throws Exception {
    if (n instanceof Ast0.Binop)    return gen((Ast0.Binop) n);
    if (n instanceof Ast0.Unop)     return gen((Ast0.Unop) n);
    if (n instanceof Ast0.Id)       return gen((Ast0.Id) n);
    if (n instanceof Ast0.IntLit)   return gen((Ast0.IntLit) n);
    if (n instanceof Ast0.BoolLit)  return gen((Ast0.BoolLit) n);
    throw new GenException("Unknown Exp node: " + n);
  }

  // Ast0.Binop ---
  // Ast0.BOP op;
  // Ast0.Exp e1,e2;
  //
  // AG:
  //   newTemp: t
  //   code: e1.c + e2.c
  //         + "t = e1.v op e2.v"
  //
  static CodePack gen(Ast0.Binop n) throws Exception {
    List<IR0.Inst> code = new ArrayList<IR0.Inst>();

    CodePack e1Expr = gen(n.e1);
    CodePack e2Expr = gen(n.e2);
    IR0.BOP op = gen(n.op);
    if(e1.src instanceof IR0.IntLit && e2.src instanceof IR0.IntLit)
    {
        switch (op){
            case ADD:
                return new CodePack(new IR0.IntLit(e1Expr.toString().valueOf() + e2Expr.toString().valueOf()), code); 
            case SUB:
                return new CodePack(new IR0.IntLit(e1Expr.toString().valueOf() - e2Expr.toString().valueOf()), code);
            case MUL:
                return new CodePack(new IR0.IntLit(e1Expr.toString().valueOf() * e2Expr.toString().valueOf()), code);
            case DIV:
                return new CodePack(new IR0.IntLit(e1Expr.toString().valueOf() / e2Expr.toString().valueOf()), code); 
            case EQ:
                if(e1Expr.toString().valueOf() == e2Expr.tostring().valueOf())
                    return new CodePack(new IR0.BoolLit(true), code); 
                else
                    return new CodePack(new IR0.BoolLit(false), code);
            case NE:
                if(e1Expr.toString().valueOf() != e2Expr.tostring().valueOf())
                    return new CodePack(new IR0.BoolLit(true), code); 
                else
                    return new CodePack(new IR0.BoolLit(false), code);
            case GT:
                if(e1Expr.toString().valueOf() > e2Expr.tostring().valueOf())
                    return new CodePack(new IR0.BoolLit(true), code); 
                else
                    return new CodePack(new IR0.BoolLit(false), code);
            case GE:
                if(e1Expr.toString().valueOf() >= e2Expr.tostring().valueOf())
                    return new CodePack(new IR0.BoolLit(true), code); 
                else
                    return new CodePack(new IR0.BoolLit(false), code);
            case LT:
                if(e1Expr.toString().valueOf() < e2Expr.tostring().valueOf())
                    return new CodePack(new IR0.BoolLit(true), code); 
                else
                    return new CodePack(new IR0.BoolLit(false), code);
            case LE:
                if(e1Expr.toString().valueOf() <= e2Expr.tostring().valueOf())
                    return new CodePack(new IR0.BoolLit(true), code); 
                else
                    return new CodePack(new IR0.BoolLit(false), code);
        }
    }
    else if(e1Expr instanceof IR0.IntLit){
        
    }
    IR0.Temp atemp = new IR0.Temp();
    code.addAll(e1Expr.code);
    code.addAll(e2Expr.code);
    code.add(new IR0.Binop(op, atemp, e1Expr.src, e2Expr.src));

    return new CodePack(atemp, code);
    // ... need code ...

  }

  // Ast0.Unop ---
  // Ast0.UOP op;
  // Ast0.Exp e;
  //
  // AG:
  //   newTemp: t
  //   code: e.c + "t = op e.v"
  //
  static CodePack gen(Ast0.Unop n) throws Exception {
    List<IR0.Inst> code = new ArrayList<IR0.Inst>();

    IR0.Temp atemp = new IR0.Temp();
    IR0.UOP op = null;
    CodePack eExpr = gen(n.e);
    code.addAll(eExpr.code);
    Ast0.UOP neg = Ast0.UOP.NEG;
    Ast0.UOP not = Ast0.UOP.NOT;
    if(n.op == neg){
        op = IR0.UOP.NEG;
        code.add(new IR0.Unop(op, atemp, eExpr.src));
    }
    else if(n.op == not){
        op = IR0.UOP.NOT;
        code.add(new IR0.Unop(op, atemp, eExpr.src));
    }

    return new CodePack(atemp, code);
    // ... need code ...

  }
  
  // Ast0.Id ---
  // String nm;
  //
  static CodePack gen(Ast0.Id n) throws Exception {

    return new CodePack(new IR0.Id(n.nm), new ArrayList<IR0.Inst>());
    // ... need code ...

  }

  // Ast0.IntLit ---
  // int i;
  //
  static CodePack gen(Ast0.IntLit n) throws Exception {

    return new CodePack(new IR0.IntLit(n.i), new ArrayList<IR0.Inst>());
    // ... need code ...

  }

  // Ast0.BoolLit ---
  // boolean b;
  //
  static CodePack gen(Ast0.BoolLit n) throws Exception {

    return new CodePack(new IR0.BoolLit(n.b), new ArrayList<IR0.Inst>());
    // ... need code ...

  }

  // OPERATORS

  static IR0.BOP gen(Ast0.BOP op) {
    IR0.BOP irOp = null;
    switch (op) {
    case ADD: irOp = IR0.AOP.ADD; break;
    case SUB: irOp = IR0.AOP.SUB; break;
    case MUL: irOp = IR0.AOP.MUL; break;
    case DIV: irOp = IR0.AOP.DIV; break;
    case AND: irOp = IR0.AOP.AND; break;
    case OR:  irOp = IR0.AOP.OR;  break;
    case EQ:  irOp = IR0.ROP.EQ;  break;
    case NE:  irOp = IR0.ROP.NE;  break;
    case LT:  irOp = IR0.ROP.LT;  break;
    case LE:  irOp = IR0.ROP.LE;  break;
    case GT:  irOp = IR0.ROP.GT;  break;
    case GE:  irOp = IR0.ROP.GE;  break;
    }
    return irOp;
  }
}
