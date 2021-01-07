# As stated in the book "The Elements of Computing": Handles the parsing of a single .vm file, and encapsulates access
# to the input code. It reads VM commands, parses them, and provides convenient access to their components. In
# addition, it removes all white space and comments.
class VMParser:

    def __init__(self):
        self.input_file_path = input_file_path
        self.file_lines = []
        self.current_line_pointer = 0

    def read_file_lines_into_a_list(self):
        try:
            with open(self.input_file_path, encoding='utf-8') as f:
                self.file_lines = f.readlines()
        except FileNotFoundError:
            raise FileNotFoundError

    def remove_whitespaces_and_newlines(self):
        new_file_lines = []
        for line in self.file_lines:
            new_file_lines.append(line.strip(' \r\n').split("//")[0])
        self.file_lines = new_file_lines

    # Returns true as long as the current command is still being processed.
    def has_more_commands(self):
        return self.current_line_pointer < len(self.file_lines)

    # Advances the command that is meant to be read in the input string
    def advance(self):
        self.current_line_pointer += 1 if self.has_more_commands() else False

    # Returns the type of the current command
    def command_type(self):
        arithmetic_commands = {'add', 'sub', 'lt', 'gt', 'and', 'or', 'neg', 'not', 'eq'}
        current_line = self.get_current_line()
        for command in arithmetic_commands:
            if current_line.startswith(command):
                return 'C_ARITHMETIC'
        if current_line.startswith('push'):
            return 'C_PUSH'
        if current_line.startswith('pop'):
            return 'C_POP'
        if current_line.startswith('label'):
            return 'C_LABEL'
        if current_line.startswith('goto'):
            return 'C_GOTO'
        if current_line.startswith('if'):
            return 'C_IF'
        if current_line.startswith('function'):
            return 'C_FUNCTION'
        if current_line.startswith('return'):
            return 'C_RETURN'
        if current_line.startswith('call'):
            return 'C_CALL'

    # Returns the first argument of the current command. In case of a C_ARITHMETIC, it returns the command itself.
    def arg1(self):
        current_line = self.file_lines[self.current_line_pointer]
        if self.command_type() == 'C_ARITHMETIC':
            return current_line.strip(' \n\r')
        elif self.command_type() == 'C_RETURN':
            return
        else:
            return current_line.split(" ")[1]

    def arg2(self):
        current_line = self.file_lines[self.current_line_pointer]
        return current_line.split(" ")[2]

    def get_current_line(self):
        return self.file_lines[self.current_line_pointer]