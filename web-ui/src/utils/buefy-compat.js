import * as buefy from 'buefy';

// Delete this file when the migration is done
export default function optOutBuefyFromVModelCompat() {
  for (const [name, exported] of Object.entries(buefy)) {
    if (name.startsWith('B') && exported && typeof exported === 'object') {
      exported.compatConfig = { ...exported.compatConfig, COMPONENT_V_MODEL: false };
    }
  }
}
