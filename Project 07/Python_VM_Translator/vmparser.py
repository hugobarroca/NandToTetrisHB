# As stated in the book "The Elements of Computing": Handles the parsing of a single .vmfile, and encapsulates access
# to the input code. It reads VM commands, parses them, and provides convenient access to their components. In
# addition, it removes all white space and comments.
class VMParser:

    # Initiates the file_contents variable with the contents of the supplied file.
    def __init__(self, input_file_path):
        try:
            with open(input_file_path, encoding='utf-8') as f:
                self.file_contents = f.read()
        except FileNotFoundError:
            print("The referenced file \"" + input_file_path
                  + "\" does not exist in the provided path. Please check for spelling errors.")

    # Returns true if there are more lines in the input, false otherwise.
    def has_more_commands(self):
        # TODO : Add in the code
        print('Hi!')

    # Advances the command that is meant to be read in the input string
    def advance(self):
        # TODO : Add in the code
        print('Hi!')

    # Returns the type of the current command
    def commandType(self):
        # TODO : Add in the code
        print('Hi!')

    # Returns the first argument of the current command
    def arg1(self):
        # TODO : Add in the code
        print('Hi!')

    # Returns the second argument of the current command
    def arg2(self):
        # TODO : Add in the code
        print('Hi!')
