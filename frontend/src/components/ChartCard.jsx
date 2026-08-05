import SectionCard from './SectionCard'

export default function ChartCard({ title, subtitle, children, height = 260, ...rest }) {
  return (
    <SectionCard title={title} subtitle={subtitle} {...rest}>
      <div style={{ width: '100%', height, minHeight: 180 }}>{children}</div>
    </SectionCard>
  )
}
