	.text
			  # _f () (a, b, c)
	.p2align 4,0x90
	.globl _f
_f:
	subq $40,%rsp
			  #  a = 0
	movq $0,%r10
	movslq 8(%rsp),%r12
			  # L:
_f_L:
			  #  b = a + 1
	movq $1,%r11
        addq %r10, %r11   #b is now in 11

			  #  c = c + b
	addq %r11,%r12

			  #  a = b * 2
	movq $2,%r13
	imulq %r11,%r13
        movslq %r13d,%r10

			  #  if a < 1000 goto L
	movq $1000,%r14
	cmpq %r14,%r10
	jl _f_L
			  #  return c
	movl %r12d, 8(%rsp)
	movslq 8(%rsp),%rax
	addq $40,%rsp
	ret
			  # Total inst cnt: 13

