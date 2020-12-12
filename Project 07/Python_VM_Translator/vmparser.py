# As stated in the book "The Elements of Computing": Handles the parsing of a single .vmfile, and encapsulates access
# to the input code. It reads VM commands, parses them, and provides convenient access to their components. In
# addition, it removes all white space and comments.
class VMParser:

    # Initiates the file_contents variable with the contents of the supplied file.
    def __init__(self, input_file_path):
        try:
            with open(input_file_path, encoding='utf-8') as f:
                self.file_lines = f.readlines()
        except FileNotFoundError:
            print("The referenced file \"" + input_file_path
                  + "\" does not exist in the provided path. Please check for spelling errors.")
        else:
            self.current_line_pointer = 0
        self.remove_white_spaces()

    # Removes whitespace from every line in the program
    def remove_white_spaces(self):
        for line in self.file_lines:
            line.strip()

    # Returns true if there are more lines in the input, false otherwise.
    def has_more_commands(self):
        if self.current_line_pointer < len(self.file_lines) - 1:
            return True
        else:
            return False

    # Advances the command that is meant to be read in the input string
    def advance(self):
        if self.current_line_pointer < len(self.file_lines) - 1:
            self.current_line_pointer += 1
        else:
            return False

    # Returns the type of the current command
    def command_type(self):
        arithmetic_commands = {'add', 'sub', 'neg'}
        current_line = self.file_lines[self.current_line_pointer]
        if current_line in arithmetic_commands:
            return 'C_ARITHMETIC'
        if current_line.startswith('push'):
            return 'C_PUSH'
        if current_line.startswith('pop'):
            return 'C_POP'

    # Returns the first argument of the current command
    def arg1(self):
        current_line = self.file_lines[self.current_line_pointer]
        return current_line.split(" ")[1]

    # Returns the first argument of the current command
    def arg2(self):
        current_line = self.file_lines(self.current_line_pointer)
        return current_line.split(" ")[2]
