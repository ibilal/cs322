	.file	"example3.c"
	.text
	.type	g.2386, @function
g.2386:
.LFB25:
	.cfi_startproc
	movl	%edi, %eax
	addl	(%r10), %eax
	imull	4(%r10), %eax
	subl	%edi, %eax
	subl	%edi, %eax
	ret
	.cfi_endproc
.LFE25:
	.size	g.2386, .-g.2386
	.globl	f
	.type	f, @function
f:
.LFB24:
	.cfi_startproc
	pushq	%rbx
	.cfi_def_cfa_offset 16
	.cfi_offset 3, -16
	subq	$16, %rsp
	.cfi_def_cfa_offset 32
	movl	%edi, (%rsp)
	movl	%esi, 4(%rsp)
	addl	%esi, %edi
	movq	%rsp, %r10
	call	g.2386
	movl	%eax, %ebx
	movq	%rsp, %r10
	movl	$0, %edi
	call	g.2386
	addl	%ebx, %eax
	addq	$16, %rsp
	.cfi_def_cfa_offset 16
	popq	%rbx
	.cfi_def_cfa_offset 8
	ret
	.cfi_endproc
.LFE24:
	.size	f, .-f
	.section	.rodata.str1.1,"aMS",@progbits,1
.LC0:
	.string	"%d\n"
	.text
	.globl	main
	.type	main, @function
main:
.LFB27:
	.cfi_startproc
	subq	$8, %rsp
	.cfi_def_cfa_offset 16
	movl	$2, %esi
	movl	$1, %edi
	call	f
	movl	%eax, %edx
	movl	$.LC0, %esi
	movl	$1, %edi
	movl	$0, %eax
	call	__printf_chk
	addq	$8, %rsp
	.cfi_def_cfa_offset 8
	ret
	.cfi_endproc
.LFE27:
	.size	main, .-main
	.ident	"GCC: (Ubuntu 4.8.4-2ubuntu1~14.04.1) 4.8.4"
	.section	.note.GNU-stack,"",@progbits
