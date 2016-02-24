	.file	"example2.c"
	.comm	a,40,32
	.text
	.globl	g
	.type	g, @function
g:
.LFB0:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	movq	%rdi, -8(%rbp)
	movq	%rsi, -16(%rbp)
	movq	-8(%rbp), %rax
	addq	$4, %rax
	movl	(%rax), %edx
	movq	-16(%rbp), %rax
	addq	$8, %rax
	movl	(%rax), %eax
	addl	%edx, %eax
	popq	%rbp
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE0:
	.size	g, .-g
	.globl	f
	.type	f, @function
f:
.LFB1:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	subq	$72, %rsp
	movl	%edi, -68(%rbp)
	movl	$0, -52(%rbp)
	jmp	.L4
.L5:
	movl	-52(%rbp), %eax
	cltq
	movl	a(,%rax,4), %edx
	movl	-52(%rbp), %eax
	cltq
	movl	-48(%rbp,%rax,4), %eax
	imull	-68(%rbp), %eax
	addl	%eax, %edx
	movl	-52(%rbp), %eax
	cltq
	movl	%edx, a(,%rax,4)
	addl	$1, -52(%rbp)
.L4:
	cmpl	$9, -52(%rbp)
	jle	.L5
	leaq	-48(%rbp), %rax
	movq	%rax, %rsi
	movl	$a, %edi
	call	g
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE1:
	.size	f, .-f
	.globl	main
	.type	main, @function
main:
.LFB2:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	subq	$16, %rsp
	movl	%edi, -4(%rbp)
	movq	%rsi, -16(%rbp)
	movl	$4, %edi
	call	f
	movl	$5, %eax
	leave
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE2:
	.size	main, .-main
	.ident	"GCC: (Ubuntu 4.8.4-2ubuntu1~14.04.1) 4.8.4"
	.section	.note.GNU-stack,"",@progbits
