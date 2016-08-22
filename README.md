# slackernews

Slackernews is a link/news aggregator for slack that picks up links people share on one or several channels of your team. It then displays them in a way similar to HN or lobste.rs.

This is good because:

1. Lets you keep up with link sharing within your team without the need to troll on several channels
1. Keeps a history of good shares
1. Allows you to promote how awesome your team is by sharing their shares whithout the need to reveal your channels' history

## Development notice

This project is still on its infant stages, so functionality and documentation is scarce. Come back in a couple of months for a more finished product if your interest is just using it.

## Running and developing

Create a `.lein-env` file in the project root containing the following:

```clojure
{:env {:database-uri "rethinkdb://your.rethinkdb.host:12312/slackernews"
       :scrapped-channels "list,of,meaningful,channels"
       :slack-token "xoxp-your-slack-token-123123123"}}
```

You'll also need a RethinkDB server somewhere handy. Follow these steps:

1. Start a rethinkdb server using `docker-compose up -d` on your project's directory
1. Access `http://$(docker-machine ip):8080` for your rethinkdb management interface
1. Create a `slackernews` database, and inside it the tables `users`, `channels`, and `messages`
1. Run by typing `lein run`, or fire up a REPL session using `lein repl` if you want to hack
1. See the fireworks, or get hacking

## Roadmap

- [x] Extract magic values into environment variables
- [x] Start using different profiles for different environments
- [x] Make program run without hacking and thinkering
- [x] Periodically update messages from slack, automatically
- [ ] Unfurl/scrap URLs on our own, instead of relying on slack to do it for us
- [ ] Blacklist certain domains or private pages (i.e. slack pages, private JIRA tickets)
- [ ] Update messages and links by using the realtime interface with slack
- [ ] Synchronise reactions and post them along with the links
- [ ] Auto tag posts based on content type or domain (e.g. `type:video` for youtube.com)
- [ ] Filtering on the web interface (by domain, by channel, by poster, by auto-generated tags)
- [ ] Search by tags, domain or link title with full text support
- [ ] RSS or Atom feed
- [ ] Users and authentication
- [ ] Admin interface to remove/moderate/tag links
- [ ] Interface to suggest tags for links

If you want to help out on the feature set, open some issues for existing or new items.

## On contributing code

Please do. You know the drill:

1. Fork it
1. Hack it
1. PR it

## License

TBA.
