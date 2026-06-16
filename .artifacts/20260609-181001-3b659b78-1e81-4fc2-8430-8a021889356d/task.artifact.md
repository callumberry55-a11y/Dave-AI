# Task Management

- [x] Research Dave's message intent implementation
- [x] Fix Dave spamming messages
- [x] Update Dave's security
- [x] Set up Google Login
- [x] Implement GDPR Data Deletion & ID Verification
- [x] Integrate Preferred Firebase Network URL
- [x] Fix Regression: Message Blocking & ID Screen Missing
- [/] Create Personalized Dev ID Registration Flow
	- [/] Research command interception and ID generation
	- [ ] Implement `setDevId` in `UserStatsRepository`
	- [ ] Add `DaveTask.CREATE_DEV_ID` and handle it in `ChatRepository`
	- [ ] Update `handleDevVerifyTask` to check user's personal `devId` from profile
	- [ ] Update `identifyCandidateTask` to recognize "axon id" or "create my dev id"
	- [ ] Verify flow by creating an ID and then verifying it
- [ ] Notify user of completion
