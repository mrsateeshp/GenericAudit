GenericAudit
============

An attempt to provide a language agnostic solution for storing & retrieving changes happened to business objects.

Demo instance:
==============
(response might be slow for the first request as heroku stops the server if it is not used for some time)

sample audit message can be sent @ https://generic-audit.herokuapp.com/testAuditMessage  (just click submit)

and search can be performed @ https://generic-audit.herokuapp.com/search (enter /user in Search Query textarea)

some sample queries (with values) below:

/user=johnf

/user/uid=123

/user=johnf/uid=123

/user=joh%/uid=123

/user=joh%/uid=123++/user=joh%/uid=789 (++ means OR)

/user=joh%/uid=123&&/user=joh%/uid=789 (&& means AND -- shows no resuls as there is no record with 789 uid)

/user=joh%/uid=123&&/user=joh%/uid=456 (&& means AND -- matches with current and old values of uid)

CI
==
https://travis-ci.org/mrsateeshp/GenericAudit

more details soon...
