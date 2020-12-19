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
            self.handle_operation('add', '+')
        if arithmetic_command.startswith('sub'):
            self.handle_operation('sub', '-')
        if arithmetic_command.startswith('eq'):
            self.handle_jump('eq', 'JEQ')
        if arithmetic_command.startswith('lt'):
            self.handle_jump('lt', 'JLT')
        if arithmetic_command.startswith('gt'):
            self.handle_jump('gt', 'JGT')
        if arithmetic_command.startswith('and'):
            self.handle_operation('and', '&')
        if arithmetic_command.startswith('or'):
            self.handle_operation('or', '|')
        if arithmetic_command.startswith('neg'):
            self.handle_negation('neg', '-')
        if arithmetic_command.startswith('not'):
            self.handle_negation('not', '!')

    def write_push_pop(self, command, segment, index):
        # TODO Missing memory segment implementations
        if command == 'C_PUSH':
            if segment == 'constant':
                self.output_content += '// push constant ' + index + '\n@' + index + '\nD=A\n@SP\nM=M+1\nA=M-1\nM=D\n'
            if segment == 'local':
                self.handle_push(segment, 'LCL', index)
            if segment == 'argument':
                self.handle_push(segment, 'ARG', index)
            if segment == 'this':
                self.handle_push(segment, 'THIS', index)
            if segment == 'that':
                self.handle_push(segment, 'THAT', index)
            if segment == 'temp':
                self.handle_push(segment, '5', index)
            if segment == 'pointer':
                self.handle_pointer_push(index)
        elif command == 'C_POP':
            if segment == 'local':
                self.handle_pop(segment, 'LCL', index)
            if segment == 'argument':
                self.handle_pop(segment, 'ARG', index)
            if segment == 'this':
                self.handle_pop(segment, 'THIS', index)
            if segment == 'that':
                self.handle_pop(segment, 'THAT', index)
            if segment == 'temp':
                self.handle_pop(segment, '5', index)
            if segment == 'pointer':
                self.handle_pointer_pop(index)
        else:
            pass

    def handle_pointer_push(self, index):
        if index == 0:
            self.output_content += '//push pointer 0\n@THIS\nD=M\n@SP\nM=M+1\nA=M-1\nM=D'
        else:
            self.output_content += '//push pointer 1\n@THAT\nD=M\n@SP\nM=M+1\nA=M-1\nM=D'

    def handle_pointer_pop(self, index):
        if index == 0:
            self.output_content += '//pop pointer 0\n@SP\nM=M-1\nD=M\n@THIS\nM=D'
        else:
            self.output_content += '//pop pointer 1\n@SP\nM=M-1\nD=M\n@THAT\nM=D'

    def handle_pop(self, segment_name, base_address, index):
        self.output_content += '//pop ' + segment_name + ' ' + index + '\n@' + base_address + '\nD=M\n@' + index +\
                               '\nD=D+A\n@SP\nM=M-1\nA=M\nD=D+M\nA=D-M\nM=D-A\n'

    def handle_push(self, segment_name, base_address, index):
        self.output_content += '//push ' + segment_name + ' ' + index + '\n@' + \
                               index + '\nD=A\n@' + base_address + '\nA=D+M\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n'

    # TODO: Improve this method.
    def handle_jump(self, jump_condition, jump_type):
        self.output_content += '// ' + jump_condition + '\n@0\nM=M-1\nA=M\nD=M\nA=A-1\nD=M-D\n@JUMP.' + \
                               str(self.jump_pointer) + '\nD;' + jump_type + '\n@0\nD=A\n@SP\nA=M-1\nM=D\n@END.' + \
                               str(self.jump_pointer) + '\n0;JMP\n(JUMP.' + \
                               str(self.jump_pointer) + ')\n@1\nD=-A\n@SP\nA=M-1\nM=D\n(END.' + \
                               str(self.jump_pointer) + ')'
        self.jump_pointer = self.jump_pointer + 1

    # TODO: Improve this method.
    def handle_operation(self, operation_type, operator):
        self.output_content += '// ' + operation_type + '\n@SP\nM=M-1\nA=M\nD=M\nA=A-1\nM=M' + operator + 'D\n'

    # TODO: Improve this method.
    def handle_negation(self, negation_type, operator):
        self.output_content += '// ' + negation_type + '\n@SP\nA=M-1\nM=' + operator + 'M\n'
