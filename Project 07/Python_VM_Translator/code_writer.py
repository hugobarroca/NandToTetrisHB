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
            self.output_content += '// The following code adds two stack items.\n' \
                                   '@0\nM = M - 1\nA = M\nD = M\nA = A - 1\nM = D + M\n'

    def write_push_pop(self):
        # TODO Write this function
        pass

