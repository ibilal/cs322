	.file	"example1.c"
	.text
	.globl	g
	.type	g, @function
g:
.LFB24:
	.cfi_startproc
	movl	$7, a(%rip)
	ret
	.cfi_endproc
.LFE24:
	.size	g, .-g
	.globl	f
	.type	f, @function
f:
.LFB25:
	.cfi_startproc
	addl	a(%rip), %edi
	subl	$6, %edi
	movl	%edi, b(%rip)
	movl	$7, a(%rip)
	leal	(%rdi,%rdi,4), %eax
	addl	%eax, %eax
	ret
	.cfi_endproc
.LFE25:
	.size	f, .-f
	.globl	main
	.type	main, @function
main:
.LFB26:
	.cfi_startproc
	movl	$4, %edi
	call	f
	addl	$5, %eax
	ret
	.cfi_endproc
.LFE26:
	.size	main, .-main
	.comm	b,4,4
	.globl	a
	.data
	.align 4
	.type	a, @object
	.size	a, 4
a:
	.long	3
	.ident	"GCC: (Ubuntu 4.8.4-2ubuntu1~14.04.1) 4.8.4"
	.section	.note.GNU-stack,"",@progbits
