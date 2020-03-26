# Remi
Remi is an Electron and ClojureScript application to handle your
reminders and sync them into your webserver.

## Usage
The app uses a PHP backend to sync your data, so if you want to build
and use this app you have to host all the php scripts in the `Backend`
folder on your webserver and replace the url string in
`ui_src/ui/backend.cljs` with your backend url.


# TODO
- add "calendar" view

