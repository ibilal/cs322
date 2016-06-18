	.file	"example2.c"
	.text
	.type	h.2233, @function
h.2233:
.LFB23:
	.cfi_startproc
	movl	%edi, %eax	# argument u
	imull	4(%r10), %eax	# multiply by y, fetched from f's frame via static link
	ret
	.cfi_endproc
.LFE23:
	.size	h.2233, .-h.2233
	.type	g.2236, @function
g.2236:
.LFB24:
	.cfi_startproc
	pushq	%rbx		# save callee-save reg
	.cfi_def_cfa_offset 16
	.cfi_offset 3, -16
	movq	%r10, %rbx	# save static link
	call	h.2233		# already arg 1 = our arg 1, static link = our static link
	movl	%eax, %edi	# h(z)
	addl	(%rbx), %edi	# add x, fetched from 's frame via static link	
	movq	%rbx, %r10	# static link = our static link
	call	h.2233	
	popq	%rbx		# restore callee-save reg
	.cfi_def_cfa_offset 8
	ret
	.cfi_endproc
.LFE24:
	.size	g.2236, .-g.2236
	.globl	f
	.type	f, @function
f:
.LFB22:
	.cfi_startproc
	pushq	%rbx		# save callee-save reg
	.cfi_def_cfa_offset 16
	.cfi_offset 3, -16
	subq	$16, %rsp	# allocate frame
	.cfi_def_cfa_offset 32
	movl	%edi, (%rsp)	# save x in frame
	movl	%esi, 4(%rsp)	# save y in frame
	addl	%esi, %edi	# arg 1 : x+y
	movq	%rsp, %r10	# pass pointer to our frame as setatic link
	call	g.2236
	movl	%eax, %ebx	# save result
	movq	%rsp, %r10	# again, pass pointer to our frame as static link
	movl	$0, %edi	# arg 1
	call	g.2236
	addl	%ebx, %eax	# add results
	addq	$16, %rsp	# pop frame
	.cfi_def_cfa_offset 16
	popq	%rbx		# restore callee-save reg
	.cfi_def_cfa_offset 8
	ret
	.cfi_endproc
.LFE22:
	.size	f, .-f
	.section	.rodata.str1.1,"aMS",@progbits,1
.LC0:
	.string	"%d\n"
	.text
	.globl	main
	.type	main, @function
main:
.LFB25:
	.cfi_startproc
	subq	$8, %rsp
	.cfi_def_cfa_offset 16
	movl	$2, %esi	# arg 2
	movl	$1, %edi	# arg 1
	call	f		
	movl	%eax, %edx	# f(1,2)
	movl	$.LC0, %esi	# ...
	movl	$1, %edi
	movl	$0, %eax
	call	__printf_chk
	addq	$8, %rsp
	.cfi_def_cfa_offset 8
	ret
	.cfi_endproc
.LFE25:
	.size	main, .-main
	.ident	"GCC: (Ubuntu/Linaro 4.6.3-1ubuntu5) 4.6.3"
	.section	.note.GNU-stack,"",@progbits
