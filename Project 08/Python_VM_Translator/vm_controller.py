from code_writer import VMCodeWriter
from vm_parser import VMParser
import sys


def quit_program(message):
    if message is not None:
        print(message)
    sys.exit()


# This class handles user input. It takes in a filepath, and if the given file is a .vm file,
# it generates the appropriate .asm files.
class VMController:
    def __init__(self):
        self.current_user_command = None

    def handle_user_input(self):
        while True:
            self.current_user_command = input("Please enter the filename: \n")
            if self.is_quit_command():
                quit_program("Exiting program.")
                sys.exit()
            else:
                self.create_assembly_file()

    def create_assembly_file(self):
        assembly_file_name = self.current_user_command.replace(".vm", ".asm")
        vm_parser = VMParser(self.current_user_command)
        assembly_writer = VMCodeWriter(assembly_file_name)
        try:
            vm_parser.read_file_lines_into_a_list()
        except FileNotFoundError:
            print('The referenced file could not be found. Check filename and path for typing errors.')
            return
        vm_parser.remove_whitespaces_and_newlines()
        while vm_parser.has_more_commands():
            vm_command_type = vm_parser.command_type()
            if vm_command_type == 'C_ARITHMETIC':
                assembly_writer.write_arithmetic(vm_parser.arg1())
            if vm_command_type == 'C_PUSH':
                assembly_writer.write_push_pop(vm_parser.command_type(), vm_parser.arg1(),
                                               vm_parser.arg2())
            if vm_command_type == 'C_POP':
                assembly_writer.write_push_pop(vm_parser.command_type(), vm_parser.arg1(),
                                               vm_parser.arg2())
            vm_parser.advance()
        assembly_writer.write_output_to_file()
        print('Requested file has been translated successfully. Quiting program.')
        sys.exit()

    def is_quit_command(self):
        quit_program_commands = {'q', 'quit', 'exit'}
        return self.current_user_command in quit_program_commands
