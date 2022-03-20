/*
    Calimero 2 - A library for KNX network access
    Copyright (c) 2020, 2022 B. Malinowsky

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    Linking this library statically or dynamically with other modules is
    making a combined work based on this library. Thus, the terms and
    conditions of the GNU General Public License cover the whole
    combination.

    As a special exception, the copyright holders of this library give you
    permission to link this library with independent modules to produce an
    executable, regardless of the license terms of these independent
    modules, and to copy and distribute the resulting executable under terms
    of your choice, provided that you also meet, for each linked independent
    module, the terms and conditions of the license of that module. An
    independent module is a module which is not derived from or based on
    this library. If you modify this library, you may extend this exception
    to your version of the library, but you are not obligated to do so. If
    you do not wish to do so, delete this exception statement from your
    version.
*/

package tuwien.auto.calimero.server;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXIllegalArgumentException;
import tuwien.auto.calimero.datapoint.StateDP;
import tuwien.auto.calimero.knxnetip.util.ServiceFamiliesDIB.ServiceFamily;
import tuwien.auto.calimero.secure.Keyring;
import tuwien.auto.calimero.server.gateway.SubnetConnector;
import tuwien.auto.calimero.server.knxnetip.KNXnetIPServer;
import tuwien.auto.calimero.server.knxnetip.RoutingServiceContainer;
import tuwien.auto.calimero.server.knxnetip.ServiceContainer;

/**
 * Configuration for a KNXnet/IP server.
 *
 * @see KNXnetIPServer#KNXnetIPServer(ServerConfiguration)
 */
public class ServerConfiguration {

	/**
	 * Contains the configuration for a service container managed by the KNXnet/IP server.
	 */
	public static final class Container {
		private static final String secureSymbol = new String(Character.toChars(0x1F512));

		// server side

		private final List<IndividualAddress> additionalAddresses;
		private final EnumSet<ServiceFamily> securedServices;
		private final Map<Integer, List<IndividualAddress>> tunnelingUsers;
		private final Keyring keyring;
		private final Map<String, byte[]> keyfile;

		// subnet side

		private final SubnetConnector connector;
		private final List<GroupAddress> groupAddressFilter;

		// both sides

		private final List<StateDP> timeServer;


		/**
		 * Creates a new service container configuration without any security.
		 *
		 * @param additionalAddresses additional addresses assigned to tunneling connections.
		 * @param connector subnet connector for that conainer
		 * @param groupAddressFilter group address filter
		 * @param timeServerDatapoints datapoints used for running a time server, empty map for no time server
		 *        functionality
		 */
		public Container(final List<IndividualAddress> additionalAddresses, final SubnetConnector connector,
				final List<GroupAddress> groupAddressFilter, final List<StateDP> timeServerDatapoints) {
			this(additionalAddresses, Map.of(), connector, groupAddressFilter, timeServerDatapoints,
					EnumSet.noneOf(ServiceFamily.class), Map.of());
		}

		/**
		 * Creates a new service container configuration with security.
		 *
		 * @param additionalAddresses additional addresses assigned to tunneling connections.
		 * @param connector subnet connector for that conainer
		 * @param groupAddressFilter group address filter
		 * @param timeServerDatapoints datapoints used for running a time server, empty map for no time server
		 *        functionality
		 * @param securedServices set of service families that should use KNX IP Secure
		 * @param keyfile map with KNX IP Secure keys/passwords, e.g., loaded from a keyfile
		 */
		public Container(final List<IndividualAddress> additionalAddresses,
				final Map<Integer, List<IndividualAddress>> tunnelingUsers, final SubnetConnector connector,
				final List<GroupAddress> groupAddressFilter, final List<StateDP> timeServerDatapoints,
				final EnumSet<ServiceFamily> securedServices, final Map<String, byte[]> keyfile) {

			this(additionalAddresses, tunnelingUsers, connector, groupAddressFilter, timeServerDatapoints,
					securedServices, keyfile, null);
		}

		/**
		 * Creates a new service container configuration with security and a keyring resource.
		 *
		 * @param additionalAddresses additional addresses assigned to tunneling connections.
		 * @param connector subnet connector for that conainer
		 * @param groupAddressFilter group address filter
		 * @param timeServerDatapoints datapoints used for running a time server, empty map for no time server
		 *        functionality
		 * @param securedServices set of service families that should use KNX IP Secure
		 * @param keyfile map with KNX IP Secure keys/passwords, e.g., loaded from a keyfile
		 * @param keyring keyring to use for this container, <code>keyfile</code> can contain the keyring password
		 */
		public Container(final List<IndividualAddress> additionalAddresses,
			final Map<Integer, List<IndividualAddress>> tunnelingUsers, final SubnetConnector connector,
			final List<GroupAddress> groupAddressFilter, final List<StateDP> timeServerDatapoints,
			final EnumSet<ServiceFamily> securedServices, final Map<String, byte[]> keyfile, final Keyring keyring) {

			this.additionalAddresses = List.copyOf(additionalAddresses);
			this.tunnelingUsers = Map.copyOf(tunnelingUsers);
			this.securedServices = EnumSet.copyOf(securedServices);
			this.keyring = keyring;
			this.keyfile = Map.copyOf(keyfile);

			this.connector = connector;
			this.groupAddressFilter = List.copyOf(groupAddressFilter);
			this.timeServer = List.copyOf(timeServerDatapoints);
		}

		@Deprecated
		Container(final List<IndividualAddress> additionalAddresses,
				final EnumSet<ServiceFamily> securedServices, final Map<Integer, List<IndividualAddress>> tunnelingUsers,
				final Keyring keyring, final Map<String, byte[]> keyfile, final SubnetConnector connector,
				final List<GroupAddress> groupAddressFilter, final List<StateDP> timeServerDatapoints) {

			this.additionalAddresses = List.copyOf(additionalAddresses);
			this.tunnelingUsers = Map.copyOf(tunnelingUsers);
			this.securedServices = EnumSet.copyOf(securedServices);
			this.keyring = keyring;
			this.keyfile = Map.copyOf(keyfile);

			this.connector = connector;
			this.groupAddressFilter = List.copyOf(groupAddressFilter);
			this.timeServer = List.copyOf(timeServerDatapoints);
		}


		public List<IndividualAddress> additionalAddresses() { return additionalAddresses; }

		public EnumSet<ServiceFamily> securedServices() { return securedServices.clone(); }

		public Map<Integer, List<IndividualAddress>> tunnelingUsers() { return tunnelingUsers; }

		public Optional<Keyring> keyring() { return Optional.ofNullable(keyring); }

		public Map<String, byte[]> keyfile() { return keyfile; }

		public SubnetConnector subnetConnector() { return connector; }

		public List<GroupAddress> groupAddressFilter() { return groupAddressFilter; }

		public List<StateDP> timeServer() { return timeServer; }

		@Override
		public String toString() {
			final ServiceContainer sc = subnetConnector().getServiceContainer();

			final String activated = sc.isActivated() ? "" : " [not activated]";
			String mcast = "disabled";
			String secureRouting = "";
			final var securedServices = securedServices();
			final var keyfile = keyfile();

			if ((sc instanceof RoutingServiceContainer)) {
				mcast = "multicast group " + ((RoutingServiceContainer) sc).routingMulticastAddress().getHostAddress();

				final boolean secureRoutingRequired = securedServices.contains(ServiceFamily.Routing);
				if (secureRoutingRequired && keyfile.getOrDefault("group.key", new byte[0]).length == 16)
					secureRouting = secureSymbol + " ";
			}
			final String type = subnetConnector().toString();
			String filter = "";
			if (!groupAddressFilter().isEmpty())
				filter = "\n\tGroup address filter " + groupAddressFilter();

			final boolean secureUnicastRequired = securedServices.contains(ServiceFamily.Tunneling);
			final String unicastSecure = secureUnicastRequired && keyfile.get("user.1") != null ? secureSymbol + " "
					: "";
			final String unicast = "" + sc.getControlEndpoint().getPort();
			// @formatter:off
			return String.format("%s%s:\n"
					+ "\tlisten on %s (%sport %s), KNX IP %srouting %s\n"
					+ "\t%s connection: %s%s",
					sc.getName(), activated, sc.networkInterface(), unicastSecure, unicast, secureRouting, mcast, type,
					sc.getMediumSettings(), filter);
			// @formatter:on
		}
	}


	private final String name;
	private final String friendly;
	private final boolean discovery;
	private final List<String> discoveryNetifs;
	private final List<String> outgoingNetifs;
	private final List<Container> containers;
	private final URI iosResource;
	private final char[] iosResourcePwd;


	/**
	 * Creates a new server configuration.
	 *
	 * @param name server name, used for logging etc.
	 * @param friendlyName friendly name used for KNXnet/IP discovery, {@code friendlyName.length < 30} ISO-8859-1 characters
	 * @param discovery enable discovery
	 * @param discoveryNetifs list of names for network interfaces KNXnet/IP discovery will listen on, also allowed are
	 *        "all" and "any"
	 * @param outgoingNetifs list of names for network interfaces KNXnet/IP discovery will send out discovery responses,
	 *        also allowed are "all" and "any"
	 * @param containers list with service container configurations
	 */
	public ServerConfiguration(final String name, final String friendlyName, final boolean discovery,
			final List<String> discoveryNetifs, final List<String> outgoingNetifs, final List<Container> containers) {
		this(name, friendlyName, discovery, discoveryNetifs, outgoingNetifs, containers, null, new char[0]);
	}

	/**
	 * Creates a new server configuration, also pointing to an interface object server resource.
	 *
	 * @param name server name, used for logging etc.
	 * @param friendlyName friendly name used for KNXnet/IP discovery, {@code friendlyName.length < 30} ISO-8859-1 characters
	 * @param discovery enable discovery
	 * @param discoveryNetifs list of names for network interfaces KNXnet/IP discovery will listen on, also allowed are
	 *        "all" and "any"
	 * @param outgoingNetifs list of names for network interfaces KNXnet/IP discovery will send out discovery responses,
	 *        also allowed are "all" and "any"
	 * @param containers list with service container configurations
	 * @param iosResource interface object server resource to be loaded by the KNXnet/IP server
	 * @param iosResourcePwd password for an encrypted interface object server resource, use {@code char[0]} if no
	 *        password is required
	 */
	public ServerConfiguration(final String name, final String friendlyName, final boolean discovery,
			final List<String> discoveryNetifs, final List<String> outgoingNetifs, final List<Container> containers,
			final URI iosResource, final char[] iosResourcePwd) {
		this.name = Objects.requireNonNull(name);
		if (!StandardCharsets.ISO_8859_1.newEncoder().canEncode(friendlyName))
			throw new KNXIllegalArgumentException("Cannot encode '" + friendlyName + "' using ISO-8859-1 charset");
		if (friendlyName.length() > 30)
			throw new KNXIllegalArgumentException("Friendly name '" + friendlyName + "' > 30 characters");

		friendly = friendlyName;
		this.discovery = discovery;
		this.discoveryNetifs = List.copyOf(discoveryNetifs);
		this.outgoingNetifs = List.copyOf(outgoingNetifs);
		this.containers = List.copyOf(containers);
		this.iosResource = iosResource;
		this.iosResourcePwd = iosResourcePwd.clone();
	}

	@Deprecated
	public ServerConfiguration(final String name, final String friendlyName, final boolean discovery,
			final List<String> discoveryNetifs, final List<String> outgoingNetifs, final URI iosResource,
			final List<Container> containers) {
		this(name, friendlyName, discovery, discoveryNetifs, outgoingNetifs, containers, iosResource, new char[0]);
	}

	public String name() { return name; }

	public String friendlyName() { return friendly; }

	public boolean runDiscovery() { return discovery; }

	public List<String> discoveryNetifs() { return discoveryNetifs; }

	public List<String> outgoingNetifs() { return outgoingNetifs; }

	public Optional<URI> iosResource() { return Optional.ofNullable(iosResource); }

	public char[] iosResourcePassword() { return iosResourcePwd.clone(); }

	public List<Container> containers() { return containers; }

	@Override
	public String toString() {
		// @formatter:off
		return String.format("%s '%s' - %s service container%s, discovery%s",
				name, friendly,
				containers.size(), containers.size() > 1 ? "s" : "",
				discovery ? ": listen on " + discoveryNetifs() + " send on " + outgoingNetifs() : " disabled");
		// @formatter:on
	}
}
