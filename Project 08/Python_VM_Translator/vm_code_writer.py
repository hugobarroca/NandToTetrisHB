# The purpose of this class is to read VM Commands and translate them into assembly code.
class VMCodeWriter:

    def __init__(self, assembly_file_name):
        self.jump_pointer = 0
        self.assembly_file_name = assembly_file_name
        self.output_content = ''
        self.current_function = ''

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
            if segment == 'static':
                self.handle_static_push(index)
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
                self.handle_temp_pop(segment, '5', index)
            if segment == 'pointer':
                self.handle_pointer_pop(index)
            if segment == 'static':
                self.handle_static_pop(index)
        else:
            pass

    def handle_pop(self, segment_name, base_address_location, index):
        self.output_content += '//pop ' + segment_name + ' ' + index + '\n@' + base_address_location + '\nD=M\n@' + index + \
                               '\nD=D+A\n@SP\nM=M-1\nA=M\nD=D+M\nA=D-M\nM=D-A\n'

    def handle_push(self, segment_name, base_address_location, index):
        self.output_content += '//push ' + segment_name + ' ' + index + '\n@' + \
                               index + '\nD=A\n@' + base_address_location + '\nA=D+M\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n'

    def handle_static_pop(self, index):
        filename = self.assembly_file_name.split('/')[-1].replace('.asm', '')
        self.output_content += '//pop static ' + index + ' \n@SP\nAM=M-1\nD=M\n@' + filename + '.' + index + '\nM=D\n'

    def handle_static_push(self, index):
        filename = self.assembly_file_name.split('/')[-1].replace('.asm', '')
        self.output_content += '//push static ' + index + '\n@' + filename + '.' + index \
                               + '\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n'

    def handle_pointer_pop(self, index):
        if index == '0':
            self.output_content += '//pop pointer 0\n@SP\nAM=M-1\nD=M\n@THIS\nM=D\n'
        else:
            self.output_content += '//pop pointer 1\n@SP\nAM=M-1\nD=M\n@THAT\nM=D\n'

    def handle_pointer_push(self, index):
        if index == '0':
            self.output_content += '//push pointer 0\n@THIS\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n'
        else:
            self.output_content += '//push pointer 1\n@THAT\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n'

    def handle_temp_pop(self, segment_name, base_address, index):
        self.output_content += '//pop ' + segment_name + ' ' + index + '\n@' + base_address + '\nD=A\n@' + index + \
                               '\nD=D+A\n@SP\nM=M-1\nA=M\nD=D+M\nA=D-M\nM=D-A\n'

    def handle_temp_push(self, segment_name, base_address, index):
        self.output_content += '//push ' + segment_name + ' ' + index + '\n@' + \
                               index + '\nD=A\n@' + base_address + '\nA=D+A\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n'

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

    def write_label(self, label):
        self.output_content += f"//label {label}\n"
        self.output_content += f"({self.current_function}${label})\n"

    def write_goto(self, label):
        self.output_content += f"//goto {label}\n"
        self.output_content += f"@{self.current_function}${label}\n0;JMP\n"

    def write_if(self, label):
        self.output_content += f"//if-goto {label}\n"
        self.output_content += f"@0\nAM=M-1\nD=M\n@{self.current_function}${label}\nD;JNE\n"

    def write_function(self, function_name, num_locals):
        self.output_content += f"// function {function_name}\n"
        self.output_content += f'({function_name})\n@0\nD=A\n'
        num_locals = int(num_locals)
        for x in range(0, num_locals):
            self.output_content += "@0\nM=M+1\nA=M-1\nM=D\n"

    def write_return(self):
        self.output_content += "// FRAME = LCL\n@LCL\nD=M\n@5\nM=D\n\n" \
                               "// RET = *(FRAME - 5)\n@5\nD=A\nD=M-D\n@6\nM=D\n\n" \
                               "// *ARG = pop()\n@0\nAM=M-1\nD=M\n@ARG\nA=M\nM=D\n\n" \
                               "// SP = ARG + 1\n@ARG\nD=M+1\n@SP\nM=D\n\n\n" \
                               "// THAT = *(FRAME-1)\n@5\nD=M\nA=D-1\nD=M\n@THAT\nM=D\n\n" \
                               "// THIS = *(FRAME-2)\n@5\nD=M\n@2\nA=D-A\nD=M\n@THIS\nM=D\n\n" \
                               "// ARG = *(FRAME-3)\n@5\nD=M\n@3\nA=D-A\nD=M\n@ARG\nM=D\n\n" \
                               "// LCL = *(FRAME-4)\n@5\nD=M\n@4\nA=D-A\nD=M\n@LCL\nM=D\n\n" \
                               "// goto RET\n@6\nA=M\n0;JMP\n"

    def write_call(self, function_name, num_args):
        self.output_content += f"// push return address\n@{self.current_function}" \
                               f"$function\nD=A\n@SP\nM=M+1\nA=M-1\nM=D\n\n" \
                               f"//push LCL\n@LCL\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n\n" \
                               f"//push ARG\n@ARG\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n\n" \
                               f"//push THIS\n@THIS\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n\n" \
                               f"//push THAT\n@THAT\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n\n" \
                               f"//ARG = SP-n-5\n@5\nD=A\n@{num_args}\nD=D+A\n@SP\nD=M-D\n\n" \
                               f"//LCL = SP\n@SP\nD=M\n@LCL\nM=D\n\n" \
                               f"//goto f\n@{function_name}$label\n0;JMP\n\n" \
                               f"//(return-address)\n({self.current_function}$function)"

    # TODO: This method writes the initial code at the beginning of each asm file that initializes the stack.
    def write_init(self):
        self.output_content += f"//Initialize stack pointer to 256\n@256\nD=A\n@SP\nM=D\n\n" \
                               f"//Initialize stack pointer to 256\n" \
                               f"// push return address\n@{self.current_function}" \
                               f"$function\nD=A\n@SP\nM=M+1\nA=M-1\nM=D\n\n" \
                               f"//push LCL\n@LCL\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n\n" \
                               f"//push ARG\n@ARG\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n\n" \
                               f"//push THIS\n@THIS\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n\n" \
                               f"//push THAT\n@THAT\nD=M\n@SP\nM=M+1\nA=M-1\nM=D\n\n" \
                               f"//ARG = SP-n-5\n@5\nD=A\n@1\nD=D+A\n@SP\nD=M-D\n\n" \
                               f"//LCL = SP\n@SP\nD=M\n@LCL\nM=D\n\n//goto f\n@Sys.init$function\n0;JMP\n\n" \
                               f"//(return-address)\n({self.current_function}$function)\n\n\n"
