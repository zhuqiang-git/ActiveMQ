
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
