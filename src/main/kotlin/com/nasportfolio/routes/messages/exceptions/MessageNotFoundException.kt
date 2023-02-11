package com.nasportfolio.routes.messages.exceptions

class MessageNotFoundException(messageId: String) : Exception(
    "Message with id $messageId not found"
)