# As stated in the book "The Elements of Computing": Handles the parsing of a single .vmfile, and encapsulates access
# to the input code. It reads VM commands, parses them, and provides convenient access to their components. In
# addition, it removes all white space and comments.
class VMParser:

    def __init__(self, input_file_path):
        self.input_file_path = input_file_path
        self.file_lines = []
        self.current_line_pointer = 0

    def read_file(self):
        try:
            with open(self.input_file_path, encoding='utf-8') as f:
                self.file_lines = f.readlines()
        except FileNotFoundError:
            print("The referenced file \"" + self.input_file_path
                  + "\" does not exist in the provided path. Please check for spelling errors.")
            raise FileNotFoundError

    def remove_whitespaces_and_newlines(self):
        new_file_lines = []
        for line in self.file_lines:
            new_file_lines.append(line.strip(' \r\n'))
        self.file_lines = new_file_lines

    # Returns true as long as the current command is still being processed.
    def has_more_commands(self):
        if self.current_line_pointer < len(self.file_lines):
            return True
        else:
            return False

    # Advances the command that is meant to be read in the input string
    def advance(self):
        if self.current_line_pointer < len(self.file_lines):
            self.current_line_pointer += 1
        else:
            return False

    # Returns the type of the current command
    def command_type(self):
        arithmetic_commands = {'add', 'sub', 'lt', 'gt', 'and', 'or', 'neg', 'not', 'eq'}
        current_line = self.get_current_line()
        if current_line in arithmetic_commands:
            return 'C_ARITHMETIC'
        if current_line.startswith('push'):
            return 'C_PUSH'
        if current_line.startswith('pop'):
            return 'C_POP'

    # Returns the first argument of the current command. In case of a C_ARITHMETIC, it returns the command itself.
    def arg1(self):
        current_line = self.file_lines[self.current_line_pointer]
        if self.command_type() == 'C_ARITHMETIC':
            return current_line.strip(' \n\r')
        elif self.command_type() == 'C_RETURN':
            return
        else:
            return current_line.split(" ")[1]

    # Returns the first argument of the current command
    def arg2(self):
        current_line = self.file_lines[self.current_line_pointer]
        return current_line.split(" ")[2]

    def get_current_line(self):
        return self.file_lines[self.current_line_pointer]