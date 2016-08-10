# slackernews

Slackernews is a link/news aggregator for slack that picks up links people share on one or several channels of your team. It then displays them in a way similar to HN or lobste.rs.

This is good because:

1. Lets you keep up with link sharing within your team without the need to troll on several channels
1. Keeps a history of good shares
1. Allows you to promote how awesome your team is by sharing their shares whithout the need to reveal your channels' history

## Development notice

This project is still on its infant stages, so functionality and documentation is scarce. Come back in a couple of months for a more finished product if your interest is just using it.

## Running

At this point you'll certainly notice that the `-main` function looks like this:

```clojure
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
```

Which ultimately means that running this won't produce many results.

This is because up until now this has been developed in a REPL driven style. So to start your program you'll have to manually perform these steps:

1. Start a rethinkdb server using `docker-compose up -d` on your project's directory
1. Access `http://$(docker-machine ip):8080` for your rethinkdb management interface
1. Create a `slackernews` database, and inside it the tables `users`, `channels`, and `messages`
1. Replace the `token` var in `src/slackernews/slack.clj` with your slack token
1. Replace the `scrapped-channels` var in `src/slackernews/scrapper.clj` with a meaningful list of channels
1. Make sure the values in `src/slackernews/db.clj` match the ones in the DB created by docker-compose
1. Fire up a REPL session
1. Navigate to the `slackernews.core` namespace
1. Use mount to start your database connection and your webserver by typing `(mount/start)`
1. At this point you should be able to populate your database with data from slack by:
    1. Using `slackernews.scrapper/fetch-users` to get users
    1. Using `slackernews.scrapper/fetch-channels` to get users
    1. Using `slackernews.scrapper/update-messages` to get and further synchronise messages
1. Get coding (phew)

## Future work

Along with an attempt to prioritisation:

1. Start using lein profiles
1. Extract magic values from code into environment variables in lein profiles
1. Code program startup in the `-main` function
1. Periodically update the message list from slack
1. Do our own unfurling of the URLs instead of relying on slack to do so, which brings a couple of problems:
    1. We can't control the title/information of the link
    1. Some links are not picked up by slack, which means they won't appear

More into the future:

1. Tests maybe
1. Use slack's realtime APIs to update messages
1. Pick up reactions to links and use it as a elaborate and convoluted ranking system
1. Enable filtering in the web interface (by poster, channel or domain)
1. Present links to the slack message on each link

## On contributing ideas

Please do, like this:

1. Open an issue with your concerns
1. ???????
1. Profit!

## On contributing code

Please do. You know the drill:

1. Fork
1. Work
1. Make a PR out of it

## License

Copyright © 2016 Talkdesk for now

Actual license will be determined at a later date.
