# The purpose of this class is to read VM Commands and translate them into assembly code.
class VMCodeWriter:

    def __init__(self, output_file):
        self.f = open(output_file, "w")

    def set_file_name(self, input_file):
        # TODO
        pass

    def write_arithmetic(self, arithmetic_command):
        # TODO Write this function
        pass

    def write_push_pop(self):
        # TODO Write this function
        pass

    def close(self):
        self.f.close()
