insert into client (ID,
                    EMAIL,
                    PASSWORD
) values (1,
          'barrientosjonah@gmail.com',
          1234);

insert into video (ID,
                  NAME,
                  LINK,
                  PATH,
                  DURATION_SECONDS,
                  VIDEO_STATE,
                  DATE,
                  CLIENT_ID
) values (1,
          'tremendoProgramador',
          'https://www.tiktok.com/@gertu0k/video/7258798530066713862',
          'C:\Users\barri\IdeaProjects\padawan-learner\meme-store\downloads\EL REY DE LOS CURRÍCULUMS Gracias @desayunosinformales12 por invitarm....mp4',
          18,
          'downloaded',
          '2025-10-20 19:42:17',
          1
);

insert into tag (ID, NAME, CLIENT_ID) values (1,'memes', 1);

insert into video_tag (VIDEO_ID, TAG_ID) values (1, 1);