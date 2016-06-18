	.text
			  # _f (a) (b, c, d, e, f)
	.p2align 4,0x90
	.globl _f
_f:
	subq $92,%rsp
	movl %edi,(%rsp)
			  #  e = 42
	movq $42,%r10
	movl %r10d,16(%rsp)
			  #  b = a * 4
	movslq (%rsp),%r10
	movq $4,%r11
	imulq %r11,%r10
	movl %r10d,4(%rsp)
			  #  goto L3
	jmp _f_L3
			  # L0:
_f_L0:
			  #  c = a + b
	movslq (%rsp),%r10
	movslq 4(%rsp),%r11
	addq %r11,%r10
	movl %r10d,8(%rsp)
			  #  if c < 100 goto L1
	movslq 8(%rsp),%r10
	movq $100,%r11
	cmpq %r11,%r10
	jl _f_L1
			  #  d = 10 + c
	movq $10,%r10
	movslq 8(%rsp),%r11
	addq %r11,%r10
	movl %r10d,12(%rsp)
			  #  e = d + d
	movslq 12(%rsp),%r10
	movslq 12(%rsp),%r11
	addq %r11,%r10
	movl %r10d,16(%rsp)
			  #  goto L2
	jmp _f_L2
			  # L1:
_f_L1:
			  #  f = c / 10
	movslq 8(%rsp),%rax
	cqto
	movq $10,%r11
	idivq %r11
	movl %eax,20(%rsp)
			  #  e = f - 40
	movslq 20(%rsp),%r10
	movq $40,%r11
	subq %r11,%r10
	movl %r10d,16(%rsp)
			  # L2:
_f_L2:
			  #  b = e - c
	movslq 16(%rsp),%r10
	movslq 8(%rsp),%r11
	subq %r11,%r10
	movl %r10d,4(%rsp)
			  # L3:
_f_L3:
			  #  if e > 0 goto L0
	movslq 16(%rsp),%r10
	movq $0,%r11
	cmpq %r11,%r10
	jg _f_L0
			  #  return e
	movslq 16(%rsp),%rax
	addq $92,%rsp
	ret
			  # Total inst cnt: 49
