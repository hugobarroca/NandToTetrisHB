from vmparser import VMParser


class Main:
    if __name__ == '__main__':
        while True:
            user_input = input("Please enter the filename: ")
            if user_input == "q":
                print("Exiting the program.")
                break
            my_parser = VMParser(user_input)
