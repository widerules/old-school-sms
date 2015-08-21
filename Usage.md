<table align='center'><tr><td>
<br>
</td></tr></table>

# Main #
The main entry point of the application.

<table align='center'><tr><td>
<img src='http://old-school-sms.googlecode.com/svn/wiki/main.png' /> <img src='http://old-school-sms.googlecode.com/svn/wiki/main2.png' />
</td></tr></table>

From there can you access the base functions of the application:
  * write new sms
  * handle existing sms
  * configure the application

## SMS Boxes ##
The application handles the next types of sms:
  * incoming (inbox)
  * outgoing (outbox)
  * draft

You can choose the group of sms to see from the main screen. The application can show all type in one list too. In the list the position of sender (receiver) of the sms shows the type of the sms:
  * left aligned: incoming
  * left aligned, bold: incoming, new (not read)
  * center aligned: saved but not sent (draft)
  * right aligned: sent sms

The list of sms don't always shows all existing sms: in the preferences can set the maximum number of sms to display on a page. You can switch in pages to display with pager buttons.

<table align='center'><tr><td>
<img src='http://old-school-sms.googlecode.com/svn/wiki/list.png' />
</td></tr></table>

Choosing an sms from the list you can open it: incoming or sent sms to view it, draft sms to continue writing.

If you longpress the sms in the list you got a context menu:
  * open: same as choosing the sms (to view or edit)
  * delete: delete the sms (be carefull!!!)
  * forward: opens new sms view with content of selected sms
  * replay: opens new sms view addressed with sender (receiver) of selected sms
  * resend: sends again the same sms to same number (if incoming sms, sends back the sms to sender)
  * copy text: copy the content of the message to clipboard
  * call number: call the number from the sms address

## Preferences ##
You can customize the aplication for your own. The preferences are separated in to groups for better usage:
  * View: Main window view preferences
    * Show message: Show message content in SMS list
    * Page size: Maximum number of SMS messages in the main screen list
  * Behavior - receiving: Behavior of application when receiving an SMS
    * Notify on new SMS: Notify if new SMS arrives
    * Popup on new SMS: Display popup window if new SMS arrives
    * Vibration Pattern: Vibration pattern of notification in ms (in format: vibrate,wait,vibrate,wait…)
    * Notification Sound: Notification Sound
  * Behavior - sending: Behavior of application when sending an SMS
    * Notify on successful send: Display notification if sending of SMS was successful
    * Notify on delivery: Display notification if SMS is delivered succesfuly
    * Vibration Pattern: Vibration pattern of notification in ms (in format: vibrate,wait,vibrate,wait…)
    * Notification Sound: Notification Sound

### Vibration pattern ###
The vibration pattern can be set to custom pattern with seting the vibration and pause times sequentialy in ms. Defaultly this pattern is 500,200,300 which meens 500ms vibration at start, than 200ms pause and again a 300ms vibration on the end of signal.

# Viewing #
You can view sent and received sms in a separated view in more comfortable layout. Sent sms than shows its status too on the bottom part of view, near the close button.

<table align='center'><tr><td>
<img src='http://old-school-sms.googlecode.com/svn/wiki/view.png' />
</td></tr></table>

# Sending #
When you create a new sms or when open an existing draft sms it opens in a new view in editable form. There can you edit the content and the receiver of the sms. The input is the android default input method with phisical or soft keyboard.

You can set the recipient of the message in two way:
  * seting the number directly: tap the recipient field and type the number into the number input popup dialog
  * from the phonebook: press the selector button to start selection. After you select the person from the phonebook you got a popup with persons phone types and numbers list. Selecting the required number from this list it will be automaticaly entered into recipient filed.

To send the message press the send button.

<table align='center'><tr><td>
<img src='http://old-school-sms.googlecode.com/svn/wiki/send.png' />
</td></tr></table>
## SMS status ##
Depending your settings (it may depend on service provider too) you can request a notification about the sending process. There is two type of status:
  * sending: it exists always independently on service provider and shows that the phone sent out the message
  * delivery: it exists only if the provider supports it and reports that the recipients phone received the sms (we have no information about the read status...)

The status is shown when you open the sent sms to view.

<table align='center'><tr><td>
<img src='http://old-school-sms.googlecode.com/svn/wiki/sent.png' />
</td></tr></table>
## Delivery status ##
# Popup #
The received sms can be shown in a special notification dialog. The popup shows the sender of message, the timestamp of receiving, the text of message and add access to some base options for quick handling:
  * reply: opens a reply sms for writing
  * delete: to delete the new sms
  * close: simply close the dialog

The popup window has an checkbox too to set the read status or leav it unread after closing the dialog.

<table align='center'><tr><td>
<img src='http://old-school-sms.googlecode.com/svn/wiki/popup.png' />
</td></tr></table>