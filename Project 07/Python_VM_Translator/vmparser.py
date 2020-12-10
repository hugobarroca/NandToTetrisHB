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

    # Returns true if there are more lines in the input, false otherwise.
    def has_more_commands(self):
        if self.current_line_pointer < len(self.file_lines):
            return True
        else:
            return False

    # Advances the command that is meant to be read in the input string
    def advance(self):
        if self.current_line_pointer < len(self.file_lines):
            return True
        else:
            return False

    # Returns the type of the current command
    def command_type(self):
        # TODO : Add in the code
        current_line = self.file_lines(self.current_line_pointer)
        if current_line == 'add':
            return 'add'


    # Returns the first argument of the current command
    def arg1(self):
        # TODO : Add in the code
        print('Hi!')

    # Returns the second argument of the current command
    def arg2(self):
        # TODO : Add in the code
        print('Hi!')
