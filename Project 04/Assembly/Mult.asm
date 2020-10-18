// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)


//result=0
@result
M=0

//i=R0
@R0
D=M
@i
M=D

//If i=0, stop
(LOOP)
@i
D=M;
@END
D;JEQ

//result = result + R1
@result
D=M
@R1
D=D+M
@result
M=D

//i--
@i
D=M
D=D-1
M=D

@LOOP
0; JMP

(END)
//R2 = Result
@result
D=M
@R2
M=D

@END
0;JMP