// Code generated by Wire protocol buffer compiler, do not edit.
// Source: app.cash.trifle.api.alpha.Certificate in public.proto
import Foundation
import Wire

/**
 * The certificate object handled by s2dk. This proto is used as a serialization/deserialization
 * mechanism for an otherwise opaque object whose representation is internal to the library only.
 * The purpose of this message is to bind a public key with a set of verified attributes about the
 * entity which controls the private key corresponding to the given public key.
 */
public struct Certificate {

    /**
     * Version describing the current format of the underlying bytes of the certificate. This tells
     * the s2dk library how to interpret the certificate bytes.
     * Required.
     */
    public var version: UInt32?
    /**
     * The current representation is fundamentally an x.509 certificate as defined in
     * https://datatracker.ietf.org/doc/html/rfc5280, with most of the fields and features ignored.
     */
    public var certificate: Data?
    public var unknownFields: Data = .init()

    public init(version: UInt32? = nil, certificate: Data? = nil) {
        self.version = version
        self.certificate = certificate
    }

}

#if !WIRE_REMOVE_EQUATABLE
extension Certificate : Equatable {
}
#endif

#if !WIRE_REMOVE_HASHABLE
extension Certificate : Hashable {
}
#endif

#if swift(>=5.5)
extension Certificate : Sendable {
}
#endif

extension Certificate : ProtoMessage {
    public static func protoMessageTypeURL() -> String {
        return "type.googleapis.com/app.cash.trifle.api.alpha.Certificate"
    }
}

extension Certificate : Proto2Codable {
    public init(from reader: ProtoReader) throws {
        var version: UInt32? = nil
        var certificate: Data? = nil

        let token = try reader.beginMessage()
        while let tag = try reader.nextTag(token: token) {
            switch tag {
            case 1: version = try reader.decode(UInt32.self)
            case 2: certificate = try reader.decode(Data.self)
            default: try reader.readUnknownField(tag: tag)
            }
        }
        self.unknownFields = try reader.endMessage(token: token)

        self.version = version
        self.certificate = certificate
    }

    public func encode(to writer: ProtoWriter) throws {
        try writer.encode(tag: 1, value: self.version)
        try writer.encode(tag: 2, value: self.certificate)
        try writer.writeUnknownFields(unknownFields)
    }
}

#if !WIRE_REMOVE_CODABLE
extension Certificate : Codable {
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: StringLiteralCodingKeys.self)
        self.version = try container.decodeIfPresent(UInt32.self, forKey: "version")
        self.certificate = try container.decodeIfPresent(stringEncoded: Data.self, forKey: "certificate")
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: StringLiteralCodingKeys.self)

        try container.encodeIfPresent(self.version, forKey: "version")
        try container.encodeIfPresent(stringEncoded: self.certificate, forKey: "certificate")
    }
}
#endif
