import sys

from vm_controller import VMController


class Main:
    if __name__ == '__main__':
        if len(sys.argv) == 1:
            print("Starting program in interactive mode!\n")
            controller = VMController()
            controller.handle_user_input()
        else:
            print("Starting program in argument-processing mode!\n")
            controller = VMController(sys.argv[1])
            controller.handle_user_input()
