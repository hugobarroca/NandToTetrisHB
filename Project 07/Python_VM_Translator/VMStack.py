class VMStack:
    def __init__(self):
        self.stack = []
        self.sp = -1

    def push(self, integer):
        self.stack.append(integer)
        self.increment_sp()

    def pop(self):
        self.decrement_sp()
        return self.stack.pop()

    def decrement_sp(self):
        if self.sp > -1:
            self.sp -= 1
        else:
            print("Cannot decrement stack pointer any further! SP already at 0.")

    def increment_sp(self):
        self.sp += 1
