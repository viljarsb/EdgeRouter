// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: MMTPMessages.proto

package Protocols.MMTP.MessageFormats;

public interface ProtocolMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ProtocolMessage)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.MessageType type = 1;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.MessageType type = 1;</code>
   * @return The type.
   */
  MessageType getType();

  /**
   * <code>bytes content = 2;</code>
   * @return The content.
   */
  com.google.protobuf.ByteString getContent();
}
