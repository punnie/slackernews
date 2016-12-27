(ns slackernews.reactor
  (:require [clojure.tools.logging :as log]
            [slackernews.db.core :as db]
            [slackernews.processor :as processor]))

; (condp = type
;         "hello"           (let [timestamp (System/currentTimeMillis)]
;                             (log/info "Connected to slack at" timestamp))
;         "pong"            (let [timestamp (System/currentTimeMillis)]
;                             (log/info "Last pong received at" timestamp))
;         "user_change"     (let [user          (:user slack-event)
;                                 user-slack-id (:id user)]
;                             (log/info "Updating user information for user" user-slack-id)
;                             (db/update-user user-slack-id user))
;         "channel_created" (let [channel          (:channel slack-event)
;                                 channel-slack-id (:id channel)]
;                             (log/info "Creating new channel" channel-slack-id)
;                             (db/create-channel channel))
;         (log/info slack-event))


; (defn accounts_changed
;   ""
;   [])

; (defn bot_added
;   ""
;   [])

; (defn bot_changed
;   ""
;   [])

; (defn channel_archive
;   ""
;   [])

; (defn channel_created
;   ""
;   [])

; (defn channel_deleted
;   ""
;   [])

; (defn channel_history_changed
;   ""
;   [])

; (defn channel_joined
;   ""
;   [])

; (defn channel_left
;   ""
;   [])

; (defn channel_marked
;   ""
;   [])

; (defn channel_rename
;   ""
;   [])

; (defn channel_unarchive
;   ""
;   [])

; (defn commands_changed
;   ""
;   [])

; (defn dnd_updated
;   ""
;   [])

; (defn dnd_updated_user
;   ""
;   [])

; (defn email_domain_changed
;   ""
;   [])

; (defn emoji_changed
;   ""
;   [])

; (defn file_change
;   ""
;   [])

; (defn file_comment_added
;   ""
;   [])

; (defn file_comment_deleted
;   ""
;   [])

; (defn file_comment_edited
;   ""
;   [])

; (defn file_created
;   ""
;   [])

; (defn file_deleted
;   ""
;   [])

; (defn file_public
;   ""
;   [])

; (defn file_shared
;   ""
;   [])

; (defn file_unshared
;   ""
;   [])

; (defn goodbye
;   ""
;   [])

; (defn group_archive
;   ""
;   [])

; (defn group_close
;   ""
;   [])

; (defn group_history_changed
;   ""
;   [])

; (defn group_joined
;   ""
;   [])

; (defn group_left
;   ""
;   [])

; (defn group_marked
;   ""
;   [])

; (defn group_open
;   ""
;   [])

; (defn group_rename
;   ""
;   [])

; (defn group_unarchive
;   ""
;   [])

(defn hello
  ""
  [{:keys [team]} _]
  (log/info "Connected to slack team" (-> team :domain)))

; (defn im_close
;   ""
;   [])

; (defn im_created
;   ""
;   [])

; (defn im_history_changed
;   ""
;   [])

; (defn im_marked
;   ""
;   [])

; (defn im_open
;   ""
;   [])

; (defn manual_presence_change
;   ""
;   [])

(defn message
  ""
  [{:keys [team]} message-event]
  (processor/process-message team message-event))

; (defn pin_added
;   ""
;   [])

; (defn pin_removed
;   ""
;   [])

(defn pong
  ""
  [& _]
  (let [time (System/currentTimeMillis)]
    (log/info "Received ping at" time)))

(defn presence_change
  ""
  [_ {:keys [presence user]}]
  (log/info "Updating presence for user" user "as being" presence)
  (db/update-user user {:presence presence}))

; (defn reaction_added
;   ""
;   [])

; (defn reaction_removed
;   ""
;   [])

; (defn reconnect_url
;   ""
;   [])

; (defn star_added
;   ""
;   [])

; (defn star_removed
;   ""
;   [])

; (defn subteam_created
;   ""
;   [])

; (defn subteam_self_added
;   ""
;   [])

; (defn subteam_self_removed
;   ""
;   [])

; (defn subteam_updated
;   ""
;   [])

; (defn team_domain_change
;   ""
;   [])

; (defn team_join
;   ""
;   [])

; (defn team_migration_started
;   ""
;   [])

; (defn team_plan_change
;   ""
;   [])

; (defn team_pref_change
;   ""
;   [])

; (defn team_profile_change
;   ""
;   [])

; (defn team_profile_delete
;   ""
;   [])

; (defn team_profile_reorder
;   ""
;   [])

; (defn team_rename
;   ""
;   [])

; (defn user_change
;   ""
;   [])

; (defn user_typing
;   ""
;   [])

