// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: SMMPMessages.proto

package Protocols.SMMP.Validator;

public interface SMMPMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:SMMPMessage)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bool isEncrypted = 1;</code>
   * @return The isEncrypted.
   */
  boolean getIsEncrypted();

  /**
   * <code>bool requiresAck = 2;</code>
   * @return The requiresAck.
   */
  boolean getRequiresAck();

  /**
   * <code>bytes signature = 3;</code>
   * @return The signature.
   */
  com.google.protobuf.ByteString getSignature();

  /**
   * <code>bytes certificate = 4;</code>
   * @return The certificate.
   */
  com.google.protobuf.ByteString getCertificate();

  /**
   * <code>bytes payload = 5;</code>
   * @return The payload.
   */
  com.google.protobuf.ByteString getPayload();
}