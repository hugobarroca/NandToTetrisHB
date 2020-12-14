from code_writer import VMCodeWriter
from vmparser import VMParser
import sys


# Quits the program
def quit_program(message):
    if message is not None:
        print(message)
    sys.exit()


# This class handles user input by setting up and calling other classes.
class VMController:
    def __init__(self):
        self.current_command = None

    # This method interprets user commands and acts on them.
    def handle_user_input(self):
        while True:
            self.current_command = input("Please enter the filename: \n")
            if self.is_quit_command():
                quit_program("Exiting program.")
                sys.exit()
            else:
                self.translate_file()

    # This method utilizes the VMParser and VMWriter classes in order to create an assembly file.
    def translate_file(self):
        output_file = self.current_command.replace(".vm", ".asm")
        my_parser = VMParser(self.current_command)
        my_writer = VMCodeWriter(output_file)
        try:
            my_parser.read_file()
        except FileNotFoundError:
            return
        my_parser.remove_white_spaces()
        while my_parser.has_more_commands():
            command_type = my_parser.command_type()
            if command_type == 'C_ARITHMETIC':
                my_writer.write_arithmetic(my_parser.arg1())
            my_parser.advance()

        my_writer.write_to_file()
        print('Requested file has been translated successfully. Quiting program.')
        sys.exit()

    # Detects if the current command is a quit command.
    def is_quit_command(self):
        accepted_strings = {'q', 'quit', 'exit'}
        if self.current_command in accepted_strings:
            return True
        return False
