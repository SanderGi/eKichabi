from ..LookupErrorScreen import LookupErrorScreen
from .BaseScreen import BaseScreen


class InputScreen(BaseScreen):

    VALIDATION_ERROR = 'has_exception'
    ERROR_MSG = 'exception_text'

    body = "Input Text"
    exception_type = Exception
    error_msg = "Validation Error on {input}"
    _has_next = True

    def render(self, session, context):
        return super(InputScreen, self).render(session, context)

    def input(self, input, session, context):
        try:
            # Attempt to validate input
            action_next = self.action(input, session, context)
        except self.exception_type as e:
            return LookupErrorScreen()

        if isinstance(action_next, BaseScreen):
            return action_next

        # returns the next_screen if the action did not return a new screen and next_screen is not None.
        return self.next_screen if self.next_screen is not None else \
            BaseScreen("Input: {} Validated: {}".format(input, action_next)
                       )  # seemingly supposed to create a screen displaying the input and "validated_input" when there's no next screen, this appears to never happen so it was never implemented, Alex replaced validated_input with action_next

    def action(self, input, context):
        # Default clean method
        return input
        