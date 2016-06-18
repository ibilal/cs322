// This is supporting software for CS322 Compilers and Language Design II
// Copyright (c) Portland State University
//---------------------------------------------------------------------------
// For CS322 W'16 (J. Li).
//

// Iman Bilal
// Homework 4: Target Code Generation
// March 6 2016
//
//
// Naive X86-64 code generator for IR1. (A starter version)
//
// - No register allocation; registers are used only as scratch storage.
//
import java.io.*;
import java.util.*;
import ir.*;

class CodeGen {
  static class GenException extends Exception {
    public GenException(String msg) { super(msg); }
  }

  public static void main(String [] args) throws Exception {
    if (args.length == 1) {
      FileInputStream stream = new FileInputStream(args[0]);
      IR1.Program p = new IR1Parser(stream).Program();
      stream.close();
      gen(p);
    } else {
      System.out.println("You must provide an input file name.");
    }
  }

  //----------------------------------------------------------------------------------
  // Global Variables
  //------------------

  // Global Constants
  static final int ARG_MAX = 6;

  // Per-program globals
  //
  static List<String> stringLiterals; 	    // collection of all string literals
  static final X86.Reg tempReg1 = X86.R10;  // two random scratch registers
  static final X86.Reg tempReg2 = X86.R11;  //

  // Per-function globals
  //
  static List<String> allVars; 	    	    // collection of all params, vars, and temps
  static int frameSize; 		    // stack frame size (in bytes)
  static String fnName; 		    // function's name

  // Return a variable's stack frame address
  //
  static X86.Mem varMem(IR1.Dest dest) throws Exception {
    int idx = allVars.indexOf(dest.toString());
    if (idx < 0)
      throw new GenException("Variable not found in allVars collection: " 
			     + dest.toString());
    int offset = idx * X86.Size.L.bytes;
    return new X86.Mem(X86.RSP, offset);
  }

  //----------------------------------------------------------------------------------
  // Gen Routines
  //--------------

  // Program ---
  // Func[] funcs;
  //
  // Guideline:
  // - generate code for each function
  // - emit all accumulated string literals
  //
  public static void gen(IR1.Program n) throws Exception { 
    stringLiterals = new ArrayList<String>();
    X86.emit0(".text");
    for (IR1.Func f: n.funcs)
      gen(f);
    int i = 0;
    for (String s: stringLiterals) {
      X86.Label lab = new X86.Label("_S" + i);
      X86.emitLabel(lab);
      X86.emitString(s);
      i++;
    }      
    System.out.print("\t\t\t  # Total inst cnt: " + X86.instCnt + "\n");
  }

  // Func ---
  // String name;
  // Var[] params;
  // Var[] locals;
  // Inst[] code;
  //
  // Guideline:
  // - count params; if there are more than 6 params, just fail
  // - initilialize the 'allVars' list to include all params and 
  //    local vars; this list will grow to include all temps later
  // - emit function's header, here is an example:
  //         .p2align 4,0x90
  //  	     .globl _main
  //     _main:
  // - allocate a frame for storing all params, vars, and temps
  //   . use inst count as a (safe) estimate for temp count
  //   . the space needed is 
  //       frameSize = (param count + var count + temp count) * 4
  //   . use the following statement to adjust the alignment need:
  //       if ((frameSize % 16) == 0) 
  //	      frameSize += 8;
  // - store the incoming actual arguments to their frame slots:
  //   . translate arg's index in the 'allVars' list to its stack 
  //     frame offset: idx * 4
  //   . pay attention to size info -- all IR1's stored values
  //     are integers; you may need to use the ultility routine
  //     X86.resize_reg()
  // - emit code for the body
  //
  static void gen(IR1.Func n) throws Exception { 
    allVars = new ArrayList<String>();
    if (n.params.length > ARG_MAX)
      throw new GenException("Function has too many paramters: " + n.params.length);
    fnName = n.gname.toString();
	//	funcList.add(fnName);
    System.out.print("\t\t\t  # " + n.header());

    // emit the function header
    X86.emit0(".p2align 4,0x90");
    X86.Label f = new X86.Label(n.gname.toString());
    X86.emit1(".globl", f);
    X86.emitLabel(f);

    // initialize allVars list to include params and local vars
    for (IR1.Id p: n.params)
      allVars.add(p.toString());
    for (IR1.Id v: n.locals)
      allVars.add(v.toString());

    // allocate a frame for storing all params, vars and temps
    frameSize = (n.params.length + n.locals.length + n.code.length) * 4;
    if((frameSize % 16) == 0)
        frameSize += 8;

    // emit allocation of frameSize for function
    X86.emit2("subq", new X86.Imm(frameSize), X86.RSP);

    // store the incoming args to their actual frame slots now
    //X86.Reg register = X86.resize_reg(X86.Size.L, tempReg1);
    for(int i = 0; i < n.params.length; ++i){
        int index = allVars.indexOf(n.params[i].s) * 4;
        X86.Reg register = X86.resize_reg(X86.Size.L, X86.argRegs[i]);
        X86.emit2("movl", register, new X86.Mem(X86.RSP, index)); 
    }

    // emit code for the body
    for (int i = 1; i <= n.code.length; i++) 
      gen(n.code[i-1]);
  }

  // INSTRUCTIONS

  static void gen(IR1.Inst n) throws Exception {
    System.out.print("\t\t\t  # " + n);
    if (n instanceof IR1.Binop) 	gen((IR1.Binop) n);
    else if (n instanceof IR1.Unop) 	gen((IR1.Unop) n);
    else if (n instanceof IR1.Move) 	gen((IR1.Move) n);
    else if (n instanceof IR1.Load) 	gen((IR1.Load) n);
    else if (n instanceof IR1.Store) 	gen((IR1.Store) n);
    else if (n instanceof IR1.LabelDec) gen((IR1.LabelDec) n);
    else if (n instanceof IR1.CJump) 	gen((IR1.CJump) n);
    else if (n instanceof IR1.Jump) 	gen((IR1.Jump) n);
    else if (n instanceof IR1.Call)     gen((IR1.Call) n);
    else if (n instanceof IR1.Return)   gen((IR1.Return) n);
    else throw new GenException("Illegal IR1 instruction: " + n);
  }

  // Binop ---
  //  BOP op;
  //  Dest dst;
  //  Src src1, src2;
  //
  // Guideline:
  // - add dst to 'allVars' if it is not not already there
  // - for arithmetic ops ADD, SUB, MUL, AND, and OR:
  //   . call to_reg() to bring both operands to registers
  //   . generate code for the Binop
  // - for DIV:
  //   . call to_reg to bring left operand to RAX, and right 
  //     operand to a temp reg
  //   . emit "cqto" (sign-extend RAX into RDX) 
  //   . emit "idivq" for the division operation (result is 
  //     in RAX)
  // - for relational ops:
  //   . emit "cmp" and "set"	
  //   . note that set takes a byte-sized register
  //   . emit "movzbl" to size-extend the result register
  // - for all cases:
  //   . emit a "mov" to move the result to dst's stack slot
  //     (pay attention to size info -- all IR1's stored values
  //      are integers)
  //
  static void gen(IR1.Binop n) throws Exception {
    // add des to allVars if it is not already there
    String varName = n.dst.toString();
    if (!allVars.contains(varName))
      allVars.add(varName);

    int index = allVars.indexOf(n.dst.toString());

    if (n.op instanceof IR1.AOP) {
      // for DIV
      if (n.op == IR1.AOP.DIV) {
            to_reg(n.src1, X86.RAX);
            X86.emit0("cqto");
            to_reg(n.src2, tempReg2);
            X86.emit1("idivq", tempReg2);
            X86.emit2("movl", X86.EAX, varMem(n.dst)); 
	}
        else {
            to_reg(n.src1, tempReg1);
            to_reg(n.src2, tempReg2);
            switch ((IR1.AOP) n.op) {
                case ADD: X86.emit2("addq", tempReg2, tempReg1); break;
                case SUB: X86.emit2("subq", tempReg2, tempReg1); break;
                case MUL: X86.emit2("imulq", tempReg2, tempReg1); break;
                case AND: X86.emit2("addq", tempReg2, tempReg1); break;
                case OR: X86.emit2("orq", tempReg2, tempReg1); break;
            }
            X86.emit2("movl", new X86.Reg(tempReg1.r , X86.Size.L), varMem(n.dst)); 
        }
    }
    else if (n.op instanceof IR1.ROP) {
        to_reg(n.src1, tempReg1);
        to_reg(n.src2, tempReg2);
        X86.emit2("cmpq", tempReg2, tempReg1);
        switch ((IR1.ROP) n.op) {
              case GT: X86.emit1("setg", new X86.Reg(tempReg1.r, X86.Size.B)); break;
              case GE: X86.emit1("setge", new X86.Reg(tempReg1.r, X86.Size.B)); break;
              case LT: X86.emit1("setl", new X86.Reg(tempReg1.r, X86.Size.B)); break;
              case LE: X86.emit1("setle", new X86.Reg(tempReg1.r, X86.Size.B)); break;
              case EQ: X86.emit1("sete", new X86.Reg(tempReg1.r, X86.Size.B)); break;
              case NE: X86.emit1("setne", new X86.Reg(tempReg1.r, X86.Size.B)); break;
        }
	X86.emit2("movzbl", new X86.Reg(tempReg1.r, X86.Size.B), new X86.Reg(tempReg1.r, X86.Size.L));
	X86.emit2("movl", new X86.Reg(tempReg1.r , X86.Size.L), varMem(n.dst)); 
    }
  }


  // Unop ---
  //  UOP op;
  //  Dest dst;
  //  Src src;
  //
  // Guideline:
  // - add dst to 'allVars' if it is not not already there
  // - call to_reg() to bring the operand to a register
  // - generate code for the op
  // - emit a "mov" to move the result to dst's stack slot
  //   (pay attention to size info)
  //  
  static void gen(IR1.Unop n) throws Exception {
    //Adding rdst to allVars if not already there
    String varName = n.dst.toString();
    if (!allVars.contains(varName))
      allVars.add(varName);

    // call to_reg()
    to_reg(n.src, tempReg1);

    // generate code for the op
    if (n.op == IR1.UOP.NOT)
      X86.emit1("notq", tempReg1);
    
    else if (n.op == IR1.UOP.NEG)
      X86.emit1("negq", tempReg1);

    // emit a mov to move the result to dst's stack slot
    X86.emit2("movl", new X86.Reg(tempReg1.r , X86.Size.L), varMem(n.dst)); 
  }

  // Move ---
  //  Dest dst;
  //  Src src;
  //
  // Guideline:
  // - add dst to 'allVars' if it is not not already there
  // - call to_reg() to generate code for the src
  // - emit a "mov" to move the result to dst's stack slot
  //  
  static void gen(IR1.Move n) throws Exception {
    //Adding dst to allVars if not already there
    String varName = n.dst.toString();
    if (!allVars.contains(varName))
      allVars.add(varName);

    X86.Mem destMem = varMem(n.dst);
    to_reg(n.src, tempReg1);
    X86.Reg register = X86.resize_reg(X86.Size.L, tempReg1);
    X86.emit2("movl", register, destMem);
  }

  // Load ---  
  //  Dest dst;
  //  Addr addr;
  //
  // Guideline:
  // - call gen_addr() to generate code for addr
  // - emit a "mov" to move the result to dst's stack slot
  //   (pay attention to size info)
  //
  static void gen(IR1.Load n) throws Exception {
    //Adding dst to allVars if not already there
    String varName = n.dst.toString();
    if (!allVars.contains(varName))
      allVars.add(varName);

    // call gen_addr()
    X86.Mem addr= gen_addr(n.addr, tempReg1);

    // emit a mov to move the result to dst's stack slot
    X86.emit2("movslq", addr, tempReg2);
    X86.emit2("movl", new X86.Reg(tempReg2.r , X86.Size.L), varMem(n.dst)); 

  }

  // Store ---  
  //  Addr addr;
  //  Src src;
  //
  // Guideline:
  // - call to_reg() to bring src to a register
  // - call gen_addr() to generate code for addr
  // - emit a "mov" (pay attention to size info)
  //
  static void gen(IR1.Store n) throws Exception {

    // call to_reg()
    to_reg(n.src, tempReg1);

    // call gen_addr()
    X86.Mem addr = gen_addr(n.addr, tempReg2);

    // emit a mov
    X86.emit2("movl", new X86.Reg(tempReg1.r , X86.Size.L), addr); 
  }

  // LabelDec ---  
  //  Label lab;
  //
  // Guideline:
  // - emit an unique label by adding func's name in
  //   front of IR1's label name
  //
  static void gen(IR1.LabelDec n) {
    X86.emitLabel(new X86.Label(fnName + "_" + n.lab.name));
  }

  // CJump ---
  //  ROP op;
  //  Src src1, src2;
  //  Label lab;
  //
  // Guideline:
  // - call to_reg() to bring both operands to registers
  // - generate a "cmp" and a jump instruction
  //   . remember: left and right are switched under gnu assembler
  //   . also, IR1 and X86 names for the cond suffixes are the same
  //
  static void gen(IR1.CJump n) throws Exception {
    // call to_reg()
    to_reg(n.src1, tempReg1);
    to_reg(n.src2, tempReg2);

    // generate a cmp and jump instruction
    X86.emit2("cmpq", tempReg2, tempReg1);
    X86.emit1("je", new X86.Label(fnName + "_" + n.lab.name));
  }	

  // Jump ---
  //  Label lab;
  //
  // Guideline:
  // - generate a "jmp" to a label
  //   . again, adding func's name in front of IR1's label name
  //
  static void gen(IR1.Jump n) throws Exception {

    // generate a jmp to a label
    X86.emit1("jmp", new X86.Label(fnName + "_" + n.lab.name));
  }	

  // Call ---
  //  String name;
  //  Src[] args;
  //  Dest rdst;
  //
  // Guideline:
  // - count args; if there are more than 6 args, just fail
  // - call to_reg to move arguments into the argument regs
  // - emit a "call" with func's name as the label
  // - if return value is expected, 
  //   . add rdst to 'allVars' if it is not not already there
  //   . emit a "mov" to move result from rax to rdst's frame slot 
  //     (pay attention to size info)
  //
  static void gen(IR1.Call n) throws Exception {
    // count args, if more than 6 then fail
    if (n.args.length > ARG_MAX)
      throw new GenException("Function has too many paramters: " + n.args.length);

    // call to_reg to move args into arg regs
    for(int i = 0; i < n.args.length; ++i){
        to_reg(n.args[i], X86.argRegs[i]);
    }

    // emit a "call" with func's name as the label
    X86.emit1("call", new X86.Label(n.gname.toString()));

    // if return is expected (rdst is not null)
    if (n.rdst != null) {
        String varName = n.rdst.toString();
        if (!allVars.contains(varName))
            allVars.add(varName);
        X86.emit2("movl", X86.EAX, varMem(n.rdst));
    }
  }

  // Return ---  
  //  Src val;
  //
  // Guideline:
  // - if there is a value, emit a "mov" to move it to rax
  // - pop the frame (add frameSize back to stack pointer)
  // - emit a "ret"
  //
  static void gen(IR1.Return n) throws Exception {

    // if there is a value, emit a mov to move it to rax
    if (n.val != null) {
      if (n.val instanceof IR1.IntLit) {
            to_reg(n.val, X86.RAX);
      }
      else {
            int index = allVars.indexOf(n.val.toString());
            X86.emit2("movslq", new X86.Mem(X86.RSP, index*4) ,X86.RAX);
      }
    }

    // pop the fram
    X86.emit2("addq", new X86.Imm(frameSize), X86.RSP);
    
    // emit a ret
    X86.emit0("ret");
  }

  // OPERANDS

  // Src -> Id | Temp | IntLit | BoolLit | StrLit 
  //
  // This routine bring the Src's value into the temp register. 
  //
  // Guideline:
  // - Id and Temp:
  //   . emit code to load the value from stack memory to the temp reg
  // - IntLit:
  //   . emit code to move the value to the temp reg
  // - BoolLit:
  //   . same as IntLit, except that use 1 for "true" and 0 for "false"
  // - StrLit:
  //   . add the string to 'stringLiterals' collection to be emitted late
  //   . construct a label "_Sn" where n is the index of the string 
  //     in the 'stringLiterals' collection
  //   . emit a "lea" to move the label to the temp reg
  //
  static void to_reg(IR1.Src n, final X86.Reg tempReg) throws Exception {

    // Id and Temp
    if (n instanceof IR1.Id || n instanceof IR1.Temp) {
      int index = allVars.indexOf(n.toString());
      X86.emit2("movslq", new X86.Mem(X86.RSP, index*4), tempReg); 
    }
    // IntLit
    if (n instanceof IR1.IntLit)
      X86.emit2("movq", new X86.Imm(((IR1.IntLit) n).i), tempReg);

    // BoolLit
    if (n instanceof IR1.BoolLit) {
      int boolValue = 0;
      if (((IR1.BoolLit) n).b) boolValue = 1;
      X86.emit2("movq", new X86.Imm(boolValue), tempReg);
    }

    // StrLit
    if (n instanceof IR1.StrLit) {
      String string = ((IR1.StrLit) n).s;
      stringLiterals.add(string);
      X86.Label label = new X86.Label("_S" + stringLiterals.indexOf(string));
      X86.emit2("leaq", new X86.AddrName(label.toString()), tempReg);
    }
  }

  // Addr ---
  // Src base;  
  // int offset;
  //
  // Guideline:
  // - call to_reg() on base to place it in a reg
  // - return a memory operand (i.e. X86.Mem) representing the address
  //
  static X86.Mem gen_addr(IR1.Addr addr, X86.Reg tempReg) throws Exception {
    to_reg(addr.base, tempReg);
    return new X86.Mem(tempReg, addr.offset);
  }
}
