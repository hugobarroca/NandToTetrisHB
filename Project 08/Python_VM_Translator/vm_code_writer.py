# The purpose of this class is to read VM Commands and translate them into assembly code.
class VMCodeWriter:

    def __init__(self, assembly_file_name):
        self.jump_pointer = 0
        self.return_counter = 0
        self.assembly_file_name = assembly_file_name
        self.output_content = ''
        self.current_function = 'null'
        self.current_file = 'null'

    def write_output_to_file(self):
        f = open(self.assembly_file_name, "w")
        f.write(self.output_content)
        f.close()

    def set_file_name(self, filename):
        self.current_file = filename

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
        self.output_content += f'//pop {segment_name} {index}\n' \
                               f'@{base_address_location}\n' \
                               f'D=M\n' \
                               f'@{index}\n' \
                               f'D=D+A\n' \
                               f'@SP\n' \
                               f'M=M-1\n' \
                               f'A=M\n' \
                               f'D=D+M\n' \
                               f'A=D-M\n' \
                               f'M=D-A\n'

    def handle_push(self, segment_name, base_address_location, index):
        self.output_content += f'//push {segment_name} {index}\n' \
                               f'@{index}\n' \
                               f'D=A\n' \
                               f'@{base_address_location}\n' \
                               f'A=D+M\n' \
                               f'D=M\n' \
                               f'@SP\n' \
                               f'M=M+1\n' \
                               f'A=M-1\n' \
                               f'M=D\n'

    def handle_static_pop(self, index):
        filename = self.current_file.split('\\')[-1].split('/')[-1].replace('.asm', '').replace('.vm', '')
        self.output_content += f'//pop static {index} \n' \
                               f'@SP\n' \
                               f'AM=M-1\n' \
                               f'D=M\n' \
                               f'@{filename}.{index}\n' \
                               f'M=D\n'

    def handle_static_push(self, index):
        filename = self.current_file.split('\\')[-1].split('/')[-1].replace('.asm', '').replace('.vm', '')
        self.output_content += f'//push static {index}\n' \
                               f'@{filename}.{index}\n' \
                               f'D=M\n' \
                               f'@SP\n' \
                               f'M=M+1\n' \
                               f'A=M-1\n' \
                               f'M=D\n'

    def handle_pointer_pop(self, index):
        if index == '0':
            self.output_content += '//pop pointer 0\n' \
                                   '@SP\n' \
                                   'AM=M-1\n' \
                                   'D=M\n' \
                                   '@THIS\n' \
                                   'M=D\n'
        else:
            self.output_content += '//pop pointer 1\n' \
                                   '@SP\n' \
                                   'AM=M-1\n' \
                                   'D=M\n' \
                                   '@THAT\n' \
                                   'M=D\n'

    def handle_pointer_push(self, index):
        if index == '0':
            self.output_content += '//push pointer 0\n' \
                                   '@THIS\n' \
                                   'D=M\n' \
                                   '@SP\n' \
                                   'M=M+1\n' \
                                   'A=M-1\n' \
                                   'M=D\n'
        else:
            self.output_content += '//push pointer 1\n' \
                                   '@THAT\n' \
                                   'D=M\n' \
                                   '@SP\n' \
                                   'M=M+1\n' \
                                   'A=M-1\n' \
                                   'M=D\n'

    def handle_temp_pop(self, segment_name, base_address, index):
        self.output_content += f'//pop {segment_name} {index}\n' \
                               f'@{base_address}\n' \
                               f'D=A\n' \
                               f'@{index}\n' \
                               f'D=D+A\n' \
                               f'@SP\n' \
                               f'M=M-1\n' \
                               f'A=M\n' \
                               f'D=D+M\n' \
                               f'A=D-M\n' \
                               f'M=D-A\n'

    def handle_temp_push(self, segment_name, base_address, index):
        self.output_content += f'//push {segment_name} {index}\n' \
                               f'@{index}\n' \
                               f'D=A\n' \
                               f'@{base_address}\n' \
                               f'A=D+A\n' \
                               f'D=M\n' \
                               f'@SP\n' \
                               f'M=M+1\n' \
                               f'A=M-1\n' \
                               f'M=D\n'

    def handle_jump(self, jump_condition, jump_type):
        self.jump_pointer = self.jump_pointer + 1
        self.output_content += f'// {jump_condition}\n' \
                               f'@0\n' \
                               f'M=M-1\n' \
                               f'A=M\n' \
                               f'D=M\n' \
                               f'A=A-1\n' \
                               f'D=M-D\n' \
                               f'@JUMP.\n' \
                               f'D;{jump_type}\n' \
                               f'@0\n' \
                               f'D=A\n' \
                               f'@SP\n' \
                               f'A=M-1\n' \
                               f'M=D\n' \
                               f'@END.\n' \
                               f'0;JMP\n' \
                               f'(JUMP.)\n' \
                               f'@1\n' \
                               f'D=-A\n' \
                               f'@SP\n' \
                               f'A=M-1\n' \
                               f'M=D\n' \
                               f'(END.)\n'

    def handle_operation(self, operation_type, operator):
        self.output_content += f'// {operation_type}\n' \
                               f'@SP\n' \
                               f'M=M-1\n' \
                               f'A=M\n' \
                               f'D=M\n' \
                               f'A=A-1\n' \
                               f'M=M{operator}D\n\n'

    def handle_negation(self, negation_type, operator):
        self.output_content += f'// {negation_type}\n' \
                               f'@SP\n' \
                               f'A=M-1\n' \
                               f'M={operator}M\n'

    def write_label(self, label):
        self.output_content += f"//label {label}\n"
        self.output_content += f"({self.current_function}${label})\n"

    def write_goto(self, label):
        self.output_content += f"//goto {label}\n"
        self.output_content += f"@{self.current_function}${label}\n" \
                               f"0;JMP\n"

    def write_if(self, label):
        self.output_content += f"//if-goto {label}\n"
        self.output_content += f"@0\n" \
                               f"AM=M-1\n" \
                               f"D=M\n" \
                               f"@{self.current_function}${label}\n" \
                               f"D;JNE\n"

    def write_function(self, function_name, num_locals):
        self.output_content += f"// function {function_name}\n"
        self.output_content += f'({function_name})\n'
        num_locals = int(num_locals)
        for x in range(0, num_locals):
            self.output_content += "@0\n" \
                                   "D=A\n" \
                                   "@SP\n" \
                                   "M=M+1\n" \
                                   "A=M-1\n" \
                                   "M=D\n"

    def write_return(self):
        self.output_content += "// FRAME = LCL\n" \
                               "@LCL\n" \
                               "D=M\n" \
                               "@R13\n" \
                               "M=D\n\n" \
                               "// RET = *(FRAME - 5)\n" \
                               "@5\n" \
                               "D=A\n" \
                               "@R13\n" \
                               "D=M-D\n" \
                               "A=D\n" \
                               "D=M\n" \
                               "@R14\n" \
                               "M=D\n\n" \
                               "// *ARG = pop()\n" \
                               "@0\n" \
                               "AM=M-1\n" \
                               "D=M\n" \
                               "@ARG\n" \
                               "A=M\n" \
                               "M=D\n\n" \
                               "// SP = ARG + 1\n" \
                               "@ARG\n" \
                               "D=M+1\n" \
                               "@SP\n" \
                               "M=D\n\n\n" \
                               "// THAT = *(FRAME-1)\n" \
                               "@R13\n" \
                               "D=M\nA=D-1\n" \
                               "D=M\n" \
                               "@THAT\n" \
                               "M=D\n\n" \
                               "// THIS = *(FRAME-2)\n" \
                               "@R13\n" \
                               "D=M\n" \
                               "@2\n" \
                               "A=D-A\n" \
                               "D=M\n" \
                               "@THIS\n" \
                               "M=D\n\n" \
                               "// ARG = *(FRAME-3)\n" \
                               "@R13\n" \
                               "D=M\n" \
                               "@3\n" \
                               "A=D-A\n" \
                               "D=M\n" \
                               "@ARG\n" \
                               "M=D\n\n" \
                               "// LCL = *(FRAME-4)\n" \
                               "@R13\n" \
                               "D=M\n" \
                               "@4\n" \
                               "A=D-A\n" \
                               "D=M\n" \
                               "@LCL\n" \
                               "M=D\n\n" \
                               "// goto RET\n" \
                               "@R14\n" \
                               "A=M\n" \
                               "0;JMP\n"

    def write_call(self, function_name, num_args):
        self.output_content += f"// push return address\n" \
                               f"@{self.current_function}$ret.{self.return_counter}\n" \
                               f"D=A\n" \
                               f"@SP\n" \
                               f"M=M+1\n" \
                               f"A=M-1\n" \
                               f"M=D\n\n" \
                               f"//push LCL\n" \
                               f"@LCL\n" \
                               f"D=M\n" \
                               f"@SP\n" \
                               f"M=M+1\n" \
                               f"A=M-1\n" \
                               f"M=D\n\n" \
                               f"//push ARG\n" \
                               f"@ARG\n" \
                               f"D=M\n" \
                               f"@SP\n" \
                               f"M=M+1\n" \
                               f"A=M-1\n" \
                               f"M=D\n\n" \
                               f"//push THIS\n" \
                               f"@THIS\n" \
                               f"D=M\n" \
                               f"@SP\n" \
                               f"M=M+1\n" \
                               f"A=M-1\n" \
                               f"M=D\n\n" \
                               f"//push THAT\n" \
                               f"@THAT\n" \
                               f"D=M\n" \
                               f"@SP\n" \
                               f"M=M+1\n" \
                               f"A=M-1\n" \
                               f"M=D\n\n" \
                               f"//ARG = SP-n-5\n" \
                               f"@5\n" \
                               f"D=A\n" \
                               f"@{num_args}\n" \
                               f"D=D+A\n" \
                               f"@SP\n" \
                               f"D=M-D\n" \
                               f"@ARG\n" \
                               f"M=D\n\n" \
                               f"//LCL = SP\n" \
                               f"@SP\n" \
                               f"D=M\n" \
                               f"@LCL\n" \
                               f"M=D\n\n" \
                               f"//goto {function_name}\n" \
                               f"@{function_name}\n" \
                               f"0;JMP\n\n" \
                               f"//(return-address)\n" \
                               f"({self.current_function}$ret.{self.return_counter})\n\n"
        self.return_counter += 1

    def write_init(self):
        self.output_content += f"//Initialize stack pointer to 256\n" \
                               f"@256\n" \
                               f"D=A\n" \
                               f"@SP\n" \
                               f"M=D\n\n" \
                               f"// push return address\n" \
                               f"@{self.current_function}$ret.0\n" \
                               f"D=A\n" \
                               f"@SP\n" \
                               f"M=M+1\n" \
                               f"A=M-1\n" \
                               f"M=D\n\n" \
                               f"//push LCL\n" \
                               f"@LCL\n" \
                               f"D=M\n" \
                               f"@SP\n" \
                               f"M=M+1\n" \
                               f"A=M-1\n" \
                               f"M=D\n\n" \
                               f"//push ARG\n" \
                               f"@ARG\n" \
                               f"D=M\n" \
                               f"@SP\n" \
                               f"M=M+1\n" \
                               f"A=M-1\n" \
                               f"M=D\n\n" \
                               f"//push THIS\n" \
                               f"@THIS\n" \
                               f"D=M\n" \
                               f"@SP\n" \
                               f"M=M+1\n" \
                               f"A=M-1\n" \
                               f"M=D\n\n" \
                               f"//push THAT\n" \
                               f"@THAT\n" \
                               f"D=M\n" \
                               f"@SP\n" \
                               f"M=M+1\n" \
                               f"A=M-1\n" \
                               f"M=D\n\n" \
                               f"//goto f\n" \
                               f"@Sys.init\n" \
                               f"0;JMP\n\n" \
                               f"//(return-address)\n" \
                               f"({self.current_function}$ret.0)\n\n"
