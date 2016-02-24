	.file	"example2.c"
	.text
	.globl	g
	.type	g, @function
g:
.LFB0:
	.cfi_startproc
	movl	4(%rdi), %eax
	addl	8(%rsi), %eax
	ret
	.cfi_endproc
.LFE0:
	.size	g, .-g
	.globl	f
	.type	f, @function
f:
.LFB1:
	.cfi_startproc
	movl	$0, %eax
.L4:
	movl	%edi, %edx
	imull	-56(%rsp,%rax), %edx
	addl	%edx, a(%rax)
	addq	$4, %rax
	cmpq	$40, %rax
	jne	.L4
	movl	a+4(%rip), %eax
	addl	-48(%rsp), %eax
	ret
	.cfi_endproc
.LFE1:
	.size	f, .-f
	.globl	main
	.type	main, @function
main:
.LFB2:
	.cfi_startproc
	subq	$8, %rsp
	.cfi_def_cfa_offset 16
	movl	$4, %edi
	call	f
	movl	$5, %eax
	addq	$8, %rsp
	.cfi_def_cfa_offset 8
	ret
	.cfi_endproc
.LFE2:
	.size	main, .-main
	.comm	a,40,32
	.ident	"GCC: (Ubuntu 4.8.4-2ubuntu1~14.04.1) 4.8.4"
	.section	.note.GNU-stack,"",@progbits
