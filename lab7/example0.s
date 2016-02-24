	.file	"example0.c"
	.comm	big_array,16777216,32
	.comm	huge_array,268435456,32
	.comm	beyond,4,4
	.comm	p1,8,8
	.comm	p2,8,8
	.comm	p3,8,8
	.comm	p4,8,8
	.text
	.globl	useless
	.type	useless, @function
useless:
.LFB2:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	movl	$0, %eax
	popq	%rbp
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE2:
	.size	useless, .-useless
	.section	.rodata
.LC0:
	.string	"\nAddress of p1: %p\n"
.LC1:
	.string	"\nAddress of p2: %p\n"
.LC2:
	.string	"\nAddress of p3: %p\n"
.LC3:
	.string	"\nAddress of p4: %p\n"
	.text
	.globl	main
	.type	main, @function
main:
.LFB3:
	.cfi_startproc
	pushq	%rbp
	.cfi_def_cfa_offset 16
	.cfi_offset 6, -16
	movq	%rsp, %rbp
	.cfi_def_cfa_register 6
	movl	$268435456, %edi
	call	malloc
	movq	%rax, p1(%rip)
	movl	$256, %edi
	call	malloc
	movq	%rax, p2(%rip)
	movl	$268435456, %edi
	call	malloc
	movq	%rax, p3(%rip)
	movl	$256, %edi
	call	malloc
	movq	%rax, p4(%rip)
	movq	p1(%rip), %rax
	movq	%rax, %rsi
	movl	$.LC0, %edi
	movl	$0, %eax
	call	printf
	movq	p2(%rip), %rax
	movq	%rax, %rsi
	movl	$.LC1, %edi
	movl	$0, %eax
	call	printf
	movq	p3(%rip), %rax
	movq	%rax, %rsi
	movl	$.LC2, %edi
	movl	$0, %eax
	call	printf
	movq	p4(%rip), %rax
	movq	%rax, %rsi
	movl	$.LC3, %edi
	movl	$0, %eax
	call	printf
	popq	%rbp
	.cfi_def_cfa 7, 8
	ret
	.cfi_endproc
.LFE3:
	.size	main, .-main
	.ident	"GCC: (Ubuntu 4.8.4-2ubuntu1~14.04.1) 4.8.4"
	.section	.note.GNU-stack,"",@progbits
