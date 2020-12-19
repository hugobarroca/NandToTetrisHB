# The purpose of this class is to read VM Commands and translate them into assembly code.
class VMCodeWriter:

    def __init__(self, assembly_file_name):
        self.jump_pointer = 0
        self.assembly_file_name = assembly_file_name
        self.output_content = ''

    def write_output_to_file(self):
        f = open(self.assembly_file_name, "w")
        f.write(self.output_content)
        f.close()

    def set_file_name(self, new_assembly_file_name):
        self.jump_pointer = 0
        self.assembly_file_name = new_assembly_file_name
        self.output_content = ''

    def write_arithmetic(self, arithmetic_command):
        if arithmetic_command.startswith('add'):
            self.output_content += '// add\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=M+D\n'
        if arithmetic_command.startswith('sub'):
            self.output_content += '// sub\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=M-D\n'
        if arithmetic_command.startswith('eq'):
            self.handle_jump('eq')
        if arithmetic_command.startswith('lt'):
            self.handle_jump('lt')
        if arithmetic_command.startswith('gt'):
            self.handle_jump('gt')
        if arithmetic_command.startswith('and'):
            self.output_content += '// and\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=D&M\n'
        if arithmetic_command.startswith('or'):
            self.output_content += '// or\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=D|M\n'
        if arithmetic_command.startswith('neg'):
            self.output_content += '// neg\n@SP\nA=M-1\nM=-M\n'
        if arithmetic_command.startswith('not'):
            self.output_content += '// not\n@SP\nA=M-1\nM=!M\n'

    def write_push_pop(self, command, segment, index):
        # TODO Missing memory segment implementations
        if command == 'C_PUSH':
            if segment == 'constant':
                self.output_content += '// push constant ' + index + '\n@' + index + '\nD=A\n@SP\nM=M+1\nA=M-1\nM=D\n'
        else:
            pass

    def handle_jump(self, jump_condition):
        if jump_condition == 'gt':
            jump_type = 'JGT'
        elif jump_condition == 'lt':
            jump_type = 'JLT'
        elif jump_condition == 'eq':
            jump_type = 'JEQ'

        self.output_content += '// ' + jump_condition + '\n@0\nM=M-1\nA=M\nD=M\nA=A-1\nD=M-D\n@JUMP.'
        self.output_content += str(self.jump_pointer) + '\nD;' + jump_type
        self.output_content += '\n@0\nD=A\n@SP\nA=M-1\nM=D\n@END.' + str(self.jump_pointer) + '\n0;JMP\n(JUMP.'
        self.output_content += str(self.jump_pointer) + ')\n@1\nD=-A\n@SP\nA=M-1\nM=D\n(END.'
        self.output_content += str(self.jump_pointer) + ')'
        self.jump_pointer = self.jump_pointer + 1
