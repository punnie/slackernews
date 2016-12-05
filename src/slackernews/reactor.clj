(ns slackernews.reactor
  (:require [clojure.tools.logging :as log]))


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
  (log/info "Connected to slack team" (-> team :slack :domain)))

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
; (defn message
;   ""
;   [])
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

; (defn pref_change
;   ""
;   [])
; (defn presence_change
;   ""
;   [])
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
