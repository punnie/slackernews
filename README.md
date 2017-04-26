# slackernews

Slackernews is a link/news aggregator for slack that picks up links people share on one or several channels of your team. It then displays them in a way similar to HN or lobste.rs.

This is good because:

1. Lets you keep up with link sharing within your team without the need to troll on several channels
1. Keeps a history of good shares
1. Allows you to promote how awesome your team is by sharing their shares whithout the need to reveal your channels' history

## Development notice

This project is still on its infant stages, so functionality and documentation is scarce. Come back in a couple of months for a more finished product if your interest is just using it.

## Installing, running and developing

Not available at the moment.

## Roadmap

- [x] Extract magic values into environment variables
- [x] Start using different profiles for different environments
- [x] Make program run without hacking and thinkering
- [ ] Periodically update messages from slack, automatically
- [ ] Unfurl/scrap URLs on our own, instead of relying on slack to do it for us
- [ ] Blacklist certain domains or private pages (i.e. slack pages, private JIRA tickets)
- [ ] Auto tag posts based on content type or domain (e.g. `type:video` for youtube.com)
- [ ] Filtering on the web interface (by domain, by channel, by poster, by auto-generated tags)
- [ ] Search by tags, domain or link title with full text support
- [ ] RSS or Atom feed
- [ ] Add to pocket buttons on every link
- [ ] Synchronise reactions and post them along with the links
- [ ] Users and authentication
- [ ] Admin interface to remove/moderate/tag links
- [ ] Interface to suggest tags for links
- [ ] ~Update messages and links by using the realtime interface with slack~

If you want to help out on the feature set, open some issues for existing or new items.

## On contributing code

Everyone is welcomed to contribute code in any way they feel they should.

1. Fork the project
1. Create a topic branch from master
1. Make some commits to improve the project
1. Push this branch to your GitHub project
1. Open a Pull Request on this project's GitHub page
1. Discuss, and optionally continue committing
1. If accepted, it will be merged to master and subsequentlly deployed

## License

Slackernews is licensed under the Eclipse Public License 1.0 (EPL). See the [LICENSE](https://github.com/punnie/slackernews/blob/master/LICENSE) file for details.
