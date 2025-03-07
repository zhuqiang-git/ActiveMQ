
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            LOG.warn("Exception caught", cause);
            NettyServerCnxn cnxn = ctx.channel().attr(CONNECTION_ATTRIBUTE).getAndSet(null);
            if (cnxn != null) {
                LOG.debug("Closing {}", cnxn);
                updateHandshakeCountIfStarted(cnxn);
                cnxn.close(ServerCnxn.DisconnectReason.CHANNEL_CLOSED_EXCEPTION);
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            try {
                if (evt == NettyServerCnxn.ReadEvent.ENABLE) {
                    LOG.debug("Received ReadEvent.ENABLE");
                    NettyServerCnxn cnxn = ctx.channel().attr(CONNECTION_ATTRIBUTE).get();
                    // TODO: Not sure if cnxn can be null here. It becomes null if channelInactive()
                    // or exceptionCaught() trigger, but it's unclear to me if userEventTriggered() can run
                    // after either of those. Check for null just to be safe ...
                    if (cnxn != null) {
                        if (cnxn.getQueuedReadableBytes() > 0) {
                            cnxn.processQueuedBuffer();
                            if (advancedFlowControlEnabled && cnxn.getQueuedReadableBytes() == 0) {
                                // trigger a read if we have consumed all
                                // backlog
                                ctx.read();
                                LOG.debug("Issued a read after queuedBuffer drained");
                            }
                        }
                    }
                    if (!advancedFlowControlEnabled) {
                        ctx.channel().config().setAutoRead(true);
                    }
                } else if (evt == NettyServerCnxn.ReadEvent.DISABLE) {
                    LOG.debug("Received ReadEvent.DISABLE");
                    ctx.channel().config().setAutoRead(false);
                }
            } finally {
                ReferenceCountUtil.release(evt);
            }
        }




	static {
		// Not using "valueOf' to avoid static init cost
		ALL = new MediaType("*", "*");
		APPLICATION_ATOM_XML = new MediaType("application", "atom+xml");
		APPLICATION_CBOR = new MediaType("application", "cbor");
		APPLICATION_FORM_URLENCODED = new MediaType("application", "x-www-form-urlencoded");
		APPLICATION_GRAPHQL = new MediaType("application", "graphql+json");
		APPLICATION_JSON = new MediaType("application", "json");
		APPLICATION_JSON_UTF8 = new MediaType("application", "json", StandardCharsets.UTF_8);
		APPLICATION_NDJSON = new MediaType("application", "x-ndjson");
		APPLICATION_OCTET_STREAM = new MediaType("application", "octet-stream");
		APPLICATION_PDF = new MediaType("application", "pdf");
		APPLICATION_PROBLEM_JSON = new MediaType("application", "problem+json");
		APPLICATION_PROBLEM_JSON_UTF8 = new MediaType("application", "problem+json", StandardCharsets.UTF_8);
		APPLICATION_PROBLEM_XML = new MediaType("application", "problem+xml");
		APPLICATION_RSS_XML = new MediaType("application", "rss+xml");
		APPLICATION_STREAM_JSON = new MediaType("application", "stream+json");
		APPLICATION_XHTML_XML = new MediaType("application", "xhtml+xml");
		APPLICATION_XML = new MediaType("application", "xml");
		IMAGE_GIF = new MediaType("image", "gif");
		IMAGE_JPEG = new MediaType("image", "jpeg");
		IMAGE_PNG = new MediaType("image", "png");
		MULTIPART_FORM_DATA = new MediaType("multipart", "form-data");
		MULTIPART_MIXED = new MediaType("multipart", "mixed");
		MULTIPART_RELATED = new MediaType("multipart", "related");
		TEXT_EVENT_STREAM = new MediaType("text", "event-stream");
		TEXT_HTML = new MediaType("text", "html");
		TEXT_MARKDOWN = new MediaType("text", "markdown");
		TEXT_PLAIN = new MediaType("text", "plain");
		TEXT_XML = new MediaType("text", "xml");
	}
