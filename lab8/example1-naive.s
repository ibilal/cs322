	.text
			  # _f () (a, b, c)
	.p2align 4,0x90
	.globl _f
_f:
	subq $40,%rsp
			  #  a = 0
	movq $0,%r10
	movl %r10d,(%rsp)
			  # L:
_f_L:
			  #  b = a + 1
	movslq (%rsp),%r10
	movq $1,%r11
	addq %r11,%r10
	movl %r10d,4(%rsp)
			  #  c = c + b
	movslq 8(%rsp),%r10
	movslq 4(%rsp),%r11
	addq %r11,%r10
	movl %r10d,8(%rsp)
			  #  a = b * 2
	movslq 4(%rsp),%r10
	movq $2,%r11
	imulq %r11,%r10
	movl %r10d,(%rsp)
			  #  if a < 1000 goto L
	movslq (%rsp),%r10
	movq $1000,%r11
	cmpq %r11,%r10
	jl _f_L
			  #  return c
	movslq 8(%rsp),%rax
	addq $40,%rsp
	ret
			  # Total inst cnt: 25
