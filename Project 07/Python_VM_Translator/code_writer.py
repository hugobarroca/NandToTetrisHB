# The purpose of this class is to read VM Commands and translate them into assembly code.
class VMCodeWriter:

    def __init__(self, output_file):
        self.output_file = output_file
        self.output_content = ''
        pass

    def write_to_file(self):
        f = open(self.output_file, "w")
        f.write(self.output_content)
        f.close()

    def set_file_name(self, input_file):
        # TODO
        pass

    def write_arithmetic(self, arithmetic_command):
        # TODO
        if arithmetic_command.startswith('add'):
            self.output_content += '// add\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=D+M\n'
        if arithmetic_command.startswith('sub'):
            self.output_content += '// add\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=D-M\n'
        if arithmetic_command.startswith('eq'):
            self.output_content += '// eq\n@0\nM=M-1\nA=M\nD=M\nA=A-1\nD=D-M\n@JUMP\nD;JEQ\n@0\nD=A\n@SP\nA=M-1\nM=D\n' \
                                   '0;JMP\n(JUMP)\n@1\nD=-A\n@SP\nA=M-1\nM=D\n(END)\n@END\n0;JMP\n'
        if arithmetic_command.startswith('lt'):
            self.output_content += '// lt\n@0\nM=M-1\nA=M\nD=M\nA=A-1\nD=D-M\n@JUMP\nD;JLT\n@0\nD=A\n@SP\nA=M-1\nM=D\n' \
                                   '0;JMP\n(JUMP)\n@1\nD=-A\n@SP\nA=M-1\nM=D\n(END)\n@END\n0;JMP\n'
        if arithmetic_command.startswith('gt'):
            self.output_content += '// gt\n@0\nM=M-1\nA=M\nD=M\nA=A-1\nD=D-M\n@JUMP\nD;JGT\n@0\nD=A\n@SP\nA=M-1\nM=D\n' \
                                   '0;JMP\n(JUMP)\n@1\nD=-A\n@SP\nA=M-1\nM=D\n(END)\n@END\n0;JMP\n'
        if arithmetic_command.startswith('and'):
            self.output_content += '// and\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=D&M\n'
        if arithmetic_command.startswith('or'):
            self.output_content += '// or\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=D&M\n'
        if arithmetic_command.startswith('neg'):
            self.output_content += '// neg\n@SP\nA=M\nM=-M\n'
        if arithmetic_command.startswith('not'):
            self.output_content += '// not\n@SP\nA=M\nM=!M\n'


    def write_push_pop(self, command, segment,index):
        # TODO Write this function
        if command == 'C_PUSH':
            if segment == 'constant':
                self.output_content += '// push constant ' + index + '\n@' + index + '\nD=A\n@SP\nM=M+1\nA=M-1\nM=D\n'
        else:
            pass

