import os

from vm_code_writer import VMCodeWriter
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
        self.current_file = None
        self.current_directory = None
        self.current_writer = None

    def handle_user_input(self):
        while True:
            self.current_user_command = input("Please enter the filename (q to quit): \n")
            if self.is_quit_command():
                quit_program("Exiting program.")
                sys.exit()
            else:
                self.handle_path(self.current_user_command)

    def handle_path(self, path):
        is_file = os.path.isfile(path)
        if is_file:
            self.create_assembly_file_from_file()
        else:
            self.current_directory = self.current_user_command
            self.create_assembly_file_from_directory()

    def create_assembly_file_from_directory(self):
        self.current_file = self.current_directory + '\\Sys.vm'
        file_exists = os.path.exists(self.current_file)
        if not file_exists:
            print(f"Main file {self.current_file} was not found!")
            return

        assembly_name = os.path.basename(self.current_directory) + '.asm'
        assembly_full_path = self.current_directory + '\\' + assembly_name

        assembly_writer = VMCodeWriter(assembly_full_path)
        self.current_writer = assembly_writer

        self.current_writer.write_init()

        for filename in os.listdir(self.current_user_command):
            if filename.endswith('.vm'):
                self.current_file = filename
                filepath = self.current_directory + "\\" + filename
                self.current_writer.set_file_name(filename)
                vm_parser = VMParser(filepath)
                self.read_write_vm_file(vm_parser)

        self.current_writer.write_output_to_file()
        print('Requested directory has been translated successfully. Quiting program.')
        sys.exit()

    def create_assembly_file_from_file(self):
        assembly_file_name = self.current_user_command.replace(".vm", ".asm")
        assembly_writer = VMCodeWriter(assembly_file_name)
        self.current_writer = assembly_writer

        vm_parser = VMParser(self.current_user_command)
        self.read_write_vm_file(vm_parser)

        assembly_writer.write_output_to_file()
        print('Requested file has been translated successfully. Quiting program.')
        sys.exit()

    def read_write_vm_file(self, vm_parser):
        try:
            vm_parser.read_file_lines_into_a_list()
        except FileNotFoundError:
            filename = vm_parser.input_file_path
            print(f'The referenced file {filename} could not be found. Check filename and path for typing errors.')
            return

        vm_parser.remove_whitespaces_and_newlines()

        while vm_parser.has_more_commands():
            vm_command_type = vm_parser.command_type()

            if vm_command_type == 'C_ARITHMETIC':
                command_name = vm_parser.arg1()
                self.current_writer.write_arithmetic(command_name)

            if vm_command_type == 'C_PUSH':
                memory_segment = vm_parser.arg1()
                memory_index = vm_parser.arg2()
                self.current_writer.write_push_pop(vm_parser.command_type(), memory_segment, memory_index)

            if vm_command_type == 'C_POP':
                memory_segment = vm_parser.arg1()
                memory_index = vm_parser.arg2()
                self.current_writer.write_push_pop(vm_parser.command_type(), memory_segment, memory_index)

            if vm_command_type == 'C_LABEL':
                label = vm_parser.arg1()
                self.current_writer.write_label(label)

            if vm_command_type == 'C_GOTO':
                label = vm_parser.arg1()
                self.current_writer.write_goto(label)

            if vm_command_type == 'C_IF':
                label = vm_parser.arg1()
                self.current_writer.write_if(label)

            if vm_command_type == 'C_FUNCTION':
                function_name = vm_parser.arg1()
                num_local_variables = vm_parser.arg2()
                self.current_writer.current_function = function_name
                self.current_writer.return_counter = 0

                self.current_writer.write_function(function_name, num_local_variables)

            if vm_command_type == 'C_RETURN':
                self.current_writer.write_return()

            if vm_command_type == 'C_CALL':
                function_name = vm_parser.arg1()
                num_argument_variables = vm_parser.arg2()
                self.current_writer.write_call(function_name, num_argument_variables)

            vm_parser.advance()

    def is_quit_command(self):
        quit_program_commands = {'q', 'quit', 'exit'}
        return self.current_user_command in quit_program_commands
