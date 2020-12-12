from code_writer import VMCodeWriter
from vmparser import VMParser


class VMController:
    def __init__(self):
        self.current_command = None

    def handle_user_input(self):
        while True:
            self.current_command = input("Please enter the filename: \n")
            if self.is_quit_command():
                print("Exiting the program.")
                break
            else:
                self.translate_file()

    def translate_file(self):
        output_file = self.current_command.replace(".vm", ".asm")
        my_parser = VMParser(self.current_command)
        my_writer = VMCodeWriter(output_file)
        while my_parser.has_more_commands():
            command_type = my_parser.command_type()
            if command_type == 'C_ARITHMETIC':
                my_writer.write_arithmetic()
            my_parser.advance()
        my_writer.close()

    def is_quit_command(self):
        accepted_strings = {'q', 'quit', 'exit'}

        if self.current_command in accepted_strings:
            return True
        return False
